package fr.superviseme.api.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import jakarta.servlet.FilterChain;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PublicRateLimitFilterTest {

    @Test
    void limiteConnexionEtPreserveAutresRoutes() throws Exception {
        PublicRateLimitFilter filter = new PublicRateLimitFilter();
        FilterChain chain = mock(FilterChain.class);

        for (int i = 0; i < 11; i++) {
            MockHttpServletRequest request =
                    new MockHttpServletRequest("POST", "/api/auth/login");
            request.setServletPath("/api/auth/login");
            request.setRemoteAddr("192.0.2.10");

            MockHttpServletResponse response =
                    new MockHttpServletResponse();

            filter.doFilter(request, response, chain);

            if (i < 10) {
                assertEquals(200, response.getStatus());
            } else {
                assertEquals(429, response.getStatus());
                assertEquals("60", response.getHeader("Retry-After"));
            }
        }

        verify(chain, times(10)).doFilter(any(), any());
    }

    @Test
    void limiteFormulairePublic() throws Exception {
        PublicRateLimitFilter filter = new PublicRateLimitFilter();
        FilterChain chain = mock(FilterChain.class);

        for (int i = 0; i < 6; i++) {
            MockHttpServletRequest request =
                    new MockHttpServletRequest("POST", "/api/public/contact");
            request.setServletPath("/api/public/contact");
            request.setRemoteAddr("192.0.2.11");

            MockHttpServletResponse response =
                    new MockHttpServletResponse();

            filter.doFilter(request, response, chain);
            assertEquals(i < 5 ? 200 : 429, response.getStatus());
        }
    }
}
