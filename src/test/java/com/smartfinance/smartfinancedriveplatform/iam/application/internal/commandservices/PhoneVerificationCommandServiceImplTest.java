package com.smartfinance.smartfinancedriveplatform.iam.application.internal.commandservices;

import com.smartfinance.smartfinancedriveplatform.iam.application.outboundservices.FirebaseTokenVerifierService;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.aggregates.PhoneVerificationSession;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.VerifyFirebasePhoneTokenCommand;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.FirebasePhoneClaims;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.PhoneVerificationResult;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.PhoneVerificationStatus;
import com.smartfinance.smartfinancedriveplatform.iam.domain.repositories.PhoneVerificationSessionRepository;
import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.ExternalServiceUnavailableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PhoneVerificationCommandServiceImpl Unit Tests")
class PhoneVerificationCommandServiceImplTest {

    @Mock
    private PhoneVerificationSessionRepository sessionRepository;

    @Mock
    private FirebaseTokenVerifierService firebaseTokenVerifierService;

    private PhoneVerificationCommandServiceImpl commandService;

    private final String normalizedPhone = "51993913924";

    @BeforeEach
    void setUp() {
        commandService = new PhoneVerificationCommandServiceImpl(sessionRepository, firebaseTokenVerifierService);
    }

    @Test
    @DisplayName("Should successfully handle VerifyFirebasePhoneTokenCommand")
    void shouldSuccessfullyVerifyFirebasePhoneToken() {
        String firebaseToken = "valid-firebase-token-xyz";
        when(firebaseTokenVerifierService.verifyToken(firebaseToken))
                .thenReturn(new FirebasePhoneClaims("google-uid-123", "+51 993913924", true));
        when(sessionRepository.save(any(PhoneVerificationSession.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PhoneVerificationResult result = commandService.handle(new VerifyFirebasePhoneTokenCommand(firebaseToken, "user-456"));

        assertNotNull(result);
        assertTrue(result.verified());
        assertEquals(normalizedPhone, result.phoneNumber());
        assertEquals(PhoneVerificationStatus.VERIFIED, result.status());
        assertNotNull(result.verificationToken());
        verify(sessionRepository).expirePendingSessions(normalizedPhone);
        verify(sessionRepository).save(any(PhoneVerificationSession.class));
    }

    @Test
    @DisplayName("Should throw DomainValidationException when Firebase token is null or blank")
    void shouldThrowWhenFirebaseTokenNullOrBlank() {
        assertThrows(DomainValidationException.class, () ->
                commandService.handle(new VerifyFirebasePhoneTokenCommand(null)));
        assertThrows(DomainValidationException.class, () ->
                commandService.handle(new VerifyFirebasePhoneTokenCommand("   ")));
    }

    @Test
    @DisplayName("Should throw ExternalServiceUnavailableException when FirebaseTokenVerifierService is null")
    void shouldThrowWhenFirebaseVerifierServiceIsNull() {
        PhoneVerificationCommandServiceImpl serviceWithoutVerifier = new PhoneVerificationCommandServiceImpl(sessionRepository, null);

        assertThrows(ExternalServiceUnavailableException.class, () ->
                serviceWithoutVerifier.handle(new VerifyFirebasePhoneTokenCommand("some-token")));
    }
}
