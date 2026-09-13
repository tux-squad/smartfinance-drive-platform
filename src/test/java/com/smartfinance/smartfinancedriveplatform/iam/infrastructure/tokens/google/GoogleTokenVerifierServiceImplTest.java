package com.smartfinance.smartfinancedriveplatform.iam.infrastructure.tokens.google;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.smartfinance.smartfinancedriveplatform.iam.application.outboundservices.tokens.GoogleTokenVerifierService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GoogleTokenVerifierServiceImpl Unit Tests")
class GoogleTokenVerifierServiceImplTest {

    @Mock
    private GoogleIdTokenVerifier verifierMock;

    private GoogleTokenVerifierServiceImpl googleTokenVerifierService;

    @BeforeEach
    void setUp() {
        googleTokenVerifierService = new GoogleTokenVerifierServiceImpl(verifierMock);
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
    @DisplayName("Should return empty when ID token is invalid or verifier returns null")
    void shouldReturnEmptyWhenTokenIsInvalid() throws Exception {
        when(verifierMock.verify(anyString())).thenReturn(null);

        Optional<GoogleTokenVerifierService.GoogleUserInfo> result = googleTokenVerifierService.verifyToken("invalid.google.token");
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should return GoogleUserInfo when ID token is valid and email is verified")
    void shouldReturnGoogleUserInfoWhenTokenIsValid() throws Exception {
        GoogleIdToken googleIdTokenMock = org.mockito.Mockito.mock(GoogleIdToken.class);
        GoogleIdToken.Payload payloadMock = new GoogleIdToken.Payload();
        payloadMock.setEmail("user@gmail.com");
        payloadMock.set("given_name", "John");
        payloadMock.set("family_name", "Doe");
        payloadMock.set("picture", "https://lh3.googleusercontent.com/a/avatar.jpg");
        payloadMock.setEmailVerified(true);

        when(googleIdTokenMock.getPayload()).thenReturn(payloadMock);
        when(verifierMock.verify("valid-google-id-token")).thenReturn(googleIdTokenMock);

        Optional<GoogleTokenVerifierService.GoogleUserInfo> result = googleTokenVerifierService.verifyToken("valid-google-id-token");

        assertTrue(result.isPresent());
        var userInfo = result.get();
        assertEquals("user@gmail.com", userInfo.email());
        assertEquals("John", userInfo.givenName());
        assertEquals("Doe", userInfo.familyName());
        assertEquals("https://lh3.googleusercontent.com/a/avatar.jpg", userInfo.pictureUrl());
        assertTrue(userInfo.emailVerified());
    }
}
