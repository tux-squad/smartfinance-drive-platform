package com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest;

import com.smartfinance.smartfinancedriveplatform.iam.application.internal.commandservices.EmailVerificationCommandService;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.SendEmailVerificationCommand;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.VerifyEmailCodeCommand;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.EmailVerificationResult;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.EmailVerificationSent;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.EmailVerificationStatus;
import com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.resources.EmailVerificationResultResource;
import com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.resources.EmailVerificationSentResource;
import com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.resources.SendEmailVerificationResource;
import com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.resources.VerifyEmailCodeResource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmailVerificationController Unit Tests")
class EmailVerificationControllerTest {

    @Mock
    private EmailVerificationCommandService emailVerificationCommandService;

    @InjectMocks
    private EmailVerificationController controller;

    @Test
    @DisplayName("Should return 200 OK when requesting email verification OTP")
    void shouldReturnOkWhenSendingEmailVerification() {
        SendEmailVerificationResource resource = new SendEmailVerificationResource("test@example.com");
        EmailVerificationSent sent = new EmailVerificationSent(
                "test@example.com", "t***t@example.com", true, 600, "Código enviado exitosamente"
        );

        when(emailVerificationCommandService.handle(any(SendEmailVerificationCommand.class))).thenReturn(sent);

        ResponseEntity<EmailVerificationSentResource> response = controller.sendEmailVerification(resource);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("test@example.com", response.getBody().email());
        assertTrue(response.getBody().sessionActive());
        assertEquals(600, response.getBody().expiresInSeconds());
    }

    @Test
    @DisplayName("Should return 200 OK with verification token when verifying code")
    void shouldReturnOkWhenVerifyingEmailCode() {
        VerifyEmailCodeResource resource = new VerifyEmailCodeResource("test@example.com", "123456");
        EmailVerificationResult result = new EmailVerificationResult(
                true, "test@example.com", EmailVerificationStatus.VERIFIED,
                Instant.now(), "token-uuid-123", "Correo verificado exitosamente"
        );

        when(emailVerificationCommandService.handle(any(VerifyEmailCodeCommand.class))).thenReturn(result);

        ResponseEntity<EmailVerificationResultResource> response = controller.verifyEmailCode(resource);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().verified());
        assertEquals("test@example.com", response.getBody().email());
        assertEquals("VERIFIED", response.getBody().status());
        assertEquals("token-uuid-123", response.getBody().verificationToken());
    }
}
