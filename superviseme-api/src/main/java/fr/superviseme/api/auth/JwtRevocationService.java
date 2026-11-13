package fr.superviseme.api.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class JwtRevocationService {

    private final JdbcTemplate jdbc;

    public JwtRevocationService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Transactional
    public void revoquer(String token, Instant expiration) {
        if (expiration == null || !expiration.isAfter(Instant.now())) {
            return;
        }

        jdbc.update(
                "INSERT INTO jwt_revocations (token_hash, expires_at) "
                + "VALUES (?, ?) ON CONFLICT (token_hash) DO NOTHING",
                empreinte(token),
                java.sql.Timestamp.from(expiration)
        );
    }

    public boolean estRevoque(String token) {
        Boolean trouve = jdbc.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM jwt_revocations "
                + "WHERE token_hash = ?)",
                Boolean.class,
                empreinte(token)
        );
        return Boolean.TRUE.equals(trouve);
    }

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void purgerExpires() {
        jdbc.update(
                "DELETE FROM jwt_revocations WHERE expires_at <= ?",
                java.sql.Timestamp.from(Instant.now())
        );
    }

    private String empreinte(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(
                    token.getBytes(StandardCharsets.UTF_8)
            );
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 indisponible", exception
            );
        }
    }
}
