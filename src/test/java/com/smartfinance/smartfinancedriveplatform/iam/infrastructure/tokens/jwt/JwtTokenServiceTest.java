package com.smartfinance.smartfinancedriveplatform.iam.infrastructure.tokens.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JwtTokenService Unit Tests")
class JwtTokenServiceTest {

    private JwtTokenService jwtTokenService;

    @BeforeEach
    void setUp() {
        jwtTokenService = new JwtTokenService();
        ReflectionTestUtils.setField(jwtTokenService, "secret", "SecretKeyForTestingJwtTokenServiceInSmartFinanceDrivePlatform2026!");
        ReflectionTestUtils.setField(jwtTokenService, "expirationMs", 3600000L); // 1 hour
    }

    @Test
    @DisplayName("Should generate valid JWT token and extract username")
    void shouldGenerateAndExtractUsername() {
        String username = "user@smartfinance.com";
        List<String> roles = List.of("ROLE_USER", "ROLE_ADMIN");

        String token = jwtTokenService.generateToken(username, roles);

        assertNotNull(token);
        assertFalse(token.isBlank());

        String extractedUsername = jwtTokenService.getUsernameFromToken(token);
        assertEquals(username, extractedUsername);
    }

    @Test
    @DisplayName("Should return true for valid token and false for invalid token")
    void shouldValidateTokensCorrectly() {
        String username = "user@smartfinance.com";
        List<String> roles = List.of("ROLE_USER");

        String token = jwtTokenService.generateToken(username, roles);

        assertTrue(jwtTokenService.validateToken(token));
        assertFalse(jwtTokenService.validateToken("invalid.token.string"));
    }
}
