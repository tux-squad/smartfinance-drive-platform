package com.smartfinance.smartfinancedriveplatform.iam.infrastructure.tokens.google;

import com.smartfinance.smartfinancedriveplatform.iam.application.outboundservices.tokens.GoogleTokenVerifierService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GoogleTokenVerifierServiceImpl Unit Tests")
class GoogleTokenVerifierServiceImplTest {

    private GoogleTokenVerifierServiceImpl googleTokenVerifierService;

    @BeforeEach
    void setUp() {
        googleTokenVerifierService = new GoogleTokenVerifierServiceImpl("test-google-client-id.apps.googleusercontent.com");
    }

    @Test
    @DisplayName("Should return empty when ID token is null")
    void shouldReturnEmptyWhenTokenIsNull() {
        Optional<GoogleTokenVerifierService.GoogleUserInfo> result = googleTokenVerifierService.verifyToken(null);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should return empty when ID token is blank")
    void shouldReturnEmptyWhenTokenIsBlank() {
        Optional<GoogleTokenVerifierService.GoogleUserInfo> result = googleTokenVerifierService.verifyToken("   ");
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should return empty when ID token is invalid or malformed")
    void shouldReturnEmptyWhenTokenIsInvalid() {
        Optional<GoogleTokenVerifierService.GoogleUserInfo> result = googleTokenVerifierService.verifyToken("invalid.google.token");
        assertTrue(result.isEmpty());
    }
}
