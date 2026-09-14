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
        ReflectionTestUtils.setField(jwtTokenService, "refreshExpirationMs", 604800000L); // 7 days
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

    @Test
    @DisplayName("Should validate typed tokens correctly and reject mismatched types")
    void shouldValidateTypedTokensCorrectly() {
        String username = "user@smartfinance.com";
        List<String> roles = List.of("ROLE_USER");

        String accessToken = jwtTokenService.generateToken(username, roles);
        String refreshToken = jwtTokenService.generateRefreshToken(username);
        String resetToken = jwtTokenService.generatePasswordResetToken(username);

        // Access token validations
        assertTrue(jwtTokenService.validateAccessToken(accessToken));
        assertFalse(jwtTokenService.validateRefreshToken(accessToken));
        assertFalse(jwtTokenService.validateResetToken(accessToken));

        // Refresh token validations
        assertTrue(jwtTokenService.validateRefreshToken(refreshToken));
        assertFalse(jwtTokenService.validateAccessToken(refreshToken));
        assertFalse(jwtTokenService.validateResetToken(refreshToken));

        // Reset token validations
        assertTrue(jwtTokenService.validateResetToken(resetToken));
        assertFalse(jwtTokenService.validateAccessToken(resetToken));
        assertFalse(jwtTokenService.validateRefreshToken(resetToken));
    }

    @Test
    @DisplayName("Should throw IllegalStateException when secret is null or shorter than 32 characters")
    void shouldThrowExceptionWhenSecretIsInvalidOrTooShort() {
        JwtTokenService service = new JwtTokenService();
        ReflectionTestUtils.setField(service, "secret", "too-short");

        assertThrows(IllegalStateException.class, service::validateSecret);
    }
}
