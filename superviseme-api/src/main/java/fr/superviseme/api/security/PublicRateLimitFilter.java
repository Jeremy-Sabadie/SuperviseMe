package fr.superviseme.api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class PublicRateLimitFilter extends OncePerRequestFilter {

    private static final long WINDOW_SECONDS = 60;
    private static final int MAX_ENTRIES = 100_000;

    private final Clock clock;
    private final ConcurrentHashMap<String, Window> windows =
            new ConcurrentHashMap<>();

    public PublicRateLimitFilter() {
        this(Clock.systemUTC());
    }

    PublicRateLimitFilter(Clock clock) {
        this.clock = clock;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain
    ) throws ServletException, IOException {

        String path = request.getServletPath();
        int limit = limitFor(path, request.getMethod());

        if (limit == 0) {
            chain.doFilter(request, response);
            return;
        }

        long now = Instant.now(clock).getEpochSecond();

        if (windows.size() >= MAX_ENTRIES) {
            windows.entrySet().removeIf(
                    entry -> entry.getValue().expiresAt <= now);
        }

        if (windows.size() >= MAX_ENTRIES) {
            response.sendError(429, "Trop de requêtes");
            return;
        }

        // Adresse distante issue du serveur HTTP, sans faire confiance
        // à un en-tête X-Forwarded-For fourni par le client.
        String key = request.getRemoteAddr() + "|" + path;

        Window window = windows.compute(key, (ignored, current) -> {
            if (current == null || current.expiresAt <= now) {
                return new Window(now + WINDOW_SECONDS);
            }
            current.count.incrementAndGet();
            return current;
        });

        if (window.count.get() > limit) {
            response.setHeader("Retry-After",
                    Long.toString(Math.max(1, window.expiresAt - now)));
            response.sendError(429, "Trop de requêtes");
            return;
        }

        chain.doFilter(request, response);
    }

    private int limitFor(String path, String method) {
        if (!"POST".equals(method)) return 0;

        return switch (path) {
            case "/api/auth/login" -> 10;
            case "/api/auth/forgot-password",
                 "/api/auth/reset-password" -> 5;
            case "/api/public/contact" -> 5;
            default -> 0;
        };
    }

    private static final class Window {
        private final long expiresAt;
        private final AtomicInteger count = new AtomicInteger(1);

        private Window(long expiresAt) {
            this.expiresAt = expiresAt;
        }
    }
}
