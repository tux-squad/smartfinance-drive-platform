package com.smartfinance.smartfinancedriveplatform.iam.infrastructure.authorization.sbc.pipeline;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@DisplayName("RateLimitingFilter Unit Tests")
class RateLimitingFilterTest {

    private RateLimitingFilter rateLimitingFilter;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        rateLimitingFilter = new RateLimitingFilter();
        filterChain = mock(FilterChain.class);
    }

    @Test
    @DisplayName("Should allow requests under limit and block 11th request with 429 for /api/v1/auth/tokens")
    void shouldRateLimitTokensEndpoint() throws ServletException, IOException {
        String path = "/api/v1/auth/tokens";

        for (int i = 1; i <= 10; i++) {
            MockHttpServletRequest request = new MockHttpServletRequest("POST", path);
            request.setRemoteAddr("192.168.1.100");
            MockHttpServletResponse response = new MockHttpServletResponse();

            rateLimitingFilter.doFilterInternal(request, response, filterChain);
            assertEquals(200, response.getStatus());
        }

        // 11th request must be blocked with HTTP 429 Too Many Requests
        MockHttpServletRequest blockedRequest = new MockHttpServletRequest("POST", path);
        blockedRequest.setRemoteAddr("192.168.1.100");
        MockHttpServletResponse blockedResponse = new MockHttpServletResponse();

        rateLimitingFilter.doFilterInternal(blockedRequest, blockedResponse, filterChain);

        assertEquals(429, blockedResponse.getStatus());
    }

    @Test
    @DisplayName("Should rate limit /api/v1/auth/password-resets endpoint")
    void shouldRateLimitPasswordResetsEndpoint() throws ServletException, IOException {
        String path = "/api/v1/auth/password-resets";

        for (int i = 1; i <= 10; i++) {
            MockHttpServletRequest request = new MockHttpServletRequest("POST", path);
            request.setRemoteAddr("10.0.0.5");
            MockHttpServletResponse response = new MockHttpServletResponse();

            rateLimitingFilter.doFilterInternal(request, response, filterChain);
            assertEquals(200, response.getStatus());
        }

        MockHttpServletRequest blockedRequest = new MockHttpServletRequest("POST", path);
        blockedRequest.setRemoteAddr("10.0.0.5");
        MockHttpServletResponse blockedResponse = new MockHttpServletResponse();

        rateLimitingFilter.doFilterInternal(blockedRequest, blockedResponse, filterChain);

        assertEquals(429, blockedResponse.getStatus());
    }
}
