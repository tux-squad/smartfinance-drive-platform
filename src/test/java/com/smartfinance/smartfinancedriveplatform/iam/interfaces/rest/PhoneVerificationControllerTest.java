package com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest;

import com.smartfinance.smartfinancedriveplatform.iam.application.internal.commandservices.PhoneVerificationCommandService;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.aggregates.PhoneVerificationSession;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.SendPhoneVerificationCodeCommand;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.VerifyFirebasePhoneTokenCommand;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.VerifyPhoneCodeCommand;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.PhoneNumber;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.PhoneVerificationResult;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.PhoneVerificationStatus;
import com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.resources.PhoneVerificationConfirmationResource;
import com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.resources.PhoneVerificationSessionResource;
import com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.resources.SendPhoneVerificationResource;
import com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.resources.VerifyFirebaseTokenResource;
import com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.resources.VerifyPhoneCodeResource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PhoneVerificationController Unit Tests")
class PhoneVerificationControllerTest {

    @Mock
    private PhoneVerificationCommandService phoneVerificationCommandService;

    @InjectMocks
    private PhoneVerificationController controller;

    @Test
    @DisplayName("Should return 200 OK with session resource when sending code")
    void shouldReturnOkWhenSendingCode() {
        SendPhoneVerificationResource resource = new SendPhoneVerificationResource("+51 993913924");
        PhoneNumber phone = new PhoneNumber("51993913924");
        Instant now = Instant.now();
        PhoneVerificationSession session = new PhoneVerificationSession(phone, "hash", now, now.plus(Duration.ofMinutes(5)));

        when(phoneVerificationCommandService.handle(any(SendPhoneVerificationCodeCommand.class))).thenReturn(session);

        ResponseEntity<PhoneVerificationSessionResource> response = controller.sendVerificationCode(resource);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("51993****24", response.getBody().phoneNumber());
        assertEquals("PENDING", response.getBody().status());
    }

    @Test
    @DisplayName("Should return 200 OK with confirmation resource when verifying code")
    void shouldReturnOkWhenVerifyingCode() {
        VerifyPhoneCodeResource resource = new VerifyPhoneCodeResource("+51 993913924", "123456");
        PhoneVerificationResult result = new PhoneVerificationResult(true, "51993913924", PhoneVerificationStatus.VERIFIED, Instant.now(), "token-123", "Phone number successfully verified");

        when(phoneVerificationCommandService.handle(any(VerifyPhoneCodeCommand.class))).thenReturn(result);

        ResponseEntity<PhoneVerificationConfirmationResource> response = controller.verifyCode(resource);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().verified());
        assertEquals("VERIFIED", response.getBody().status());
    }

    @Test
    @DisplayName("Should return 200 OK with confirmation resource when verifying Firebase token")
    void shouldReturnOkWhenVerifyingFirebaseToken() {
        VerifyFirebaseTokenResource resource = new VerifyFirebaseTokenResource("sample-firebase-id-token");
        PhoneVerificationResult result = new PhoneVerificationResult(
                true, "51993913924", PhoneVerificationStatus.VERIFIED, Instant.now(), "firebase-verified-token-abc", "Verified via Firebase");

        when(phoneVerificationCommandService.handle(any(VerifyFirebasePhoneTokenCommand.class))).thenReturn(result);

        ResponseEntity<PhoneVerificationConfirmationResource> response = controller.verifyFirebaseToken(resource);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().verified());
        assertEquals("51993913924", response.getBody().phoneNumber());
        assertEquals("firebase-verified-token-abc", response.getBody().verificationToken());
    }
}
