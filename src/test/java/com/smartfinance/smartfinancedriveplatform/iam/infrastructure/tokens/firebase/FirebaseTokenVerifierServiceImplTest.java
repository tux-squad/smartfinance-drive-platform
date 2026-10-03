package com.smartfinance.smartfinancedriveplatform.iam.infrastructure.tokens.firebase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.FirebasePhoneClaims;
import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.ExternalServiceUnavailableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FirebaseTokenVerifierServiceImpl Unit Tests")
class FirebaseTokenVerifierServiceImplTest {

    @Mock
    private FirebaseAuth firebaseAuth;

    @Mock
    private FirebaseToken firebaseToken;

    private FirebaseTokenVerifierServiceImpl verifierService;

    @BeforeEach
    void setUp() {
        verifierService = new FirebaseTokenVerifierServiceImpl(firebaseAuth);
    }

    @Test
    @DisplayName("Should successfully verify token and return FirebasePhoneClaims when phone_number claim is present")
    void shouldSuccessfullyVerifyToken() throws Exception {
        String rawToken = "valid-firebase-jwt-token";
        when(firebaseAuth.verifyIdToken(rawToken)).thenReturn(firebaseToken);
        when(firebaseToken.getUid()).thenReturn("firebase-uid-12345");
        when(firebaseToken.getClaims()).thenReturn(Map.of("phone_number", "+51993913924"));

        FirebasePhoneClaims claims = verifierService.verifyToken(rawToken);

        assertNotNull(claims);
        assertEquals("firebase-uid-12345", claims.uid());
        assertEquals("+51993913924", claims.phoneNumber());
        assertTrue(claims.phoneVerified());
    }

    @Test
    @DisplayName("Should throw DomainValidationException when token is null or blank")
    void shouldThrowWhenTokenNullOrBlank() {
        assertThrows(DomainValidationException.class, () -> verifierService.verifyToken(null));
        assertThrows(DomainValidationException.class, () -> verifierService.verifyToken("   "));
    }

    @Test
    @DisplayName("Should throw ExternalServiceUnavailableException when FirebaseAuth is null")
    void shouldThrowWhenFirebaseAuthIsNull() {
        FirebaseTokenVerifierServiceImpl uninitializedService = new FirebaseTokenVerifierServiceImpl(null);
        assertThrows(ExternalServiceUnavailableException.class, () -> uninitializedService.verifyToken("some-token"));
    }

    @Test
    @DisplayName("Should throw DomainValidationException when token does not contain phone_number claim")
    void shouldThrowWhenNoPhoneNumberClaim() throws Exception {
        String rawToken = "token-without-phone";
        when(firebaseAuth.verifyIdToken(rawToken)).thenReturn(firebaseToken);
        when(firebaseToken.getUid()).thenReturn("uid-without-phone");
        when(firebaseToken.getClaims()).thenReturn(Map.of());

        assertThrows(DomainValidationException.class, () -> verifierService.verifyToken(rawToken));
    }

    @Test
    @DisplayName("Should throw DomainValidationException when FirebaseAuthException is thrown")
    void shouldThrowDomainValidationExceptionOnFirebaseAuthException() throws Exception {
        String invalidToken = "invalid-token";
        FirebaseAuthException authException = mock(FirebaseAuthException.class);
        when(authException.getMessage()).thenReturn("Invalid token signature");
        when(firebaseAuth.verifyIdToken(invalidToken)).thenThrow(authException);

        assertThrows(DomainValidationException.class, () -> verifierService.verifyToken(invalidToken));
    }

    @Test
    @DisplayName("Should throw ExternalServiceUnavailableException on unexpected network/server errors")
    void shouldThrowExternalServiceUnavailableOnUnexpectedException() throws Exception {
        String token = "network-fail-token";
        when(firebaseAuth.verifyIdToken(token)).thenThrow(new RuntimeException("Connection timed out"));

        assertThrows(ExternalServiceUnavailableException.class, () -> verifierService.verifyToken(token));
    }
}
