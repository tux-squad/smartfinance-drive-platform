package com.smartfinance.smartfinancedriveplatform.iam.application.internal.commandservices;

import com.smartfinance.smartfinancedriveplatform.iam.application.outboundservices.EmailSenderService;
import com.smartfinance.smartfinancedriveplatform.iam.application.outboundservices.EmailValidationService;
import com.smartfinance.smartfinancedriveplatform.iam.application.outboundservices.OtpGeneratorService;
import com.smartfinance.smartfinancedriveplatform.iam.application.outboundservices.dto.EmailValidationResultDto;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.aggregates.EmailVerificationSession;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.SendEmailVerificationCommand;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.VerifyEmailCodeCommand;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.EmailVerificationResult;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.EmailVerificationSent;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.EmailVerificationStatus;
import com.smartfinance.smartfinancedriveplatform.iam.domain.repositories.EmailVerificationSessionRepository;
import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmailVerificationCommandServiceImpl Unit Tests")
class EmailVerificationCommandServiceImplTest {

    @Mock
    private EmailVerificationSessionRepository sessionRepository;

    @Mock
    private EmailSenderService emailSenderService;

    @Mock
    private OtpGeneratorService otpGeneratorService;

    @Mock
    private EmailValidationService emailValidationService;

    @InjectMocks
    private EmailVerificationCommandServiceImpl commandService;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        lenient().when(emailValidationService.validateEmail(anyString()))
                .thenAnswer(invocation -> EmailValidationResultDto.fallbackValid(invocation.getArgument(0)));
    }

    @Test
    @DisplayName("Should dispatch email OTP verification successfully")
    void shouldDispatchEmailOtpVerificationSuccessfully() {
        SendEmailVerificationCommand command = new SendEmailVerificationCommand("aldospeedcuber@gmail.com");

        when(emailValidationService.validateEmail("aldospeedcuber@gmail.com"))
                .thenReturn(EmailValidationResultDto.valid("aldospeedcuber@gmail.com", "valid", "permitted"));
        when(sessionRepository.countRecentSessionsByEmail(eq("aldospeedcuber@gmail.com"), any(Instant.class))).thenReturn(0L);
        when(sessionRepository.findLatestActiveSession("aldospeedcuber@gmail.com")).thenReturn(Optional.empty());
        when(otpGeneratorService.generateOtp()).thenReturn("123456");
        when(otpGeneratorService.hashOtp("123456")).thenReturn("hashed-otp-123456");
        when(sessionRepository.save(any(EmailVerificationSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EmailVerificationSent result = commandService.handle(command);

        assertNotNull(result);
        assertEquals("aldospeedcuber@gmail.com", result.email());
        assertTrue(result.sessionActive());
        assertEquals(600, result.expiresInSeconds());
        verify(emailSenderService, times(1)).sendEmailVerificationOtp(eq("aldospeedcuber@gmail.com"), eq("123456"), eq(10));
        verify(sessionRepository, times(1)).expirePendingSessions("aldospeedcuber@gmail.com");
    }

    @Test
    @DisplayName("Should throw exception when email is rejected by EmailVerify.io")
    void shouldThrowExceptionWhenEmailIsRejectedByEmailVerify() {
        SendEmailVerificationCommand command = new SendEmailVerificationCommand("fake-nonexistent-123987@gmail.com");

        when(emailValidationService.validateEmail("fake-nonexistent-123987@gmail.com"))
                .thenReturn(new EmailValidationResultDto("fake-nonexistent-123987@gmail.com", "invalid", "mailbox_not_found", false));

        DomainValidationException ex = assertThrows(DomainValidationException.class, () -> commandService.handle(command));
        assertEquals("iam.error.email.undeliverable", ex.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when email format is invalid")
    void shouldThrowExceptionWhenEmailIsInvalid() {
        assertThrows(DomainValidationException.class, () -> commandService.handle(new SendEmailVerificationCommand("")));
        assertThrows(DomainValidationException.class, () -> commandService.handle(new SendEmailVerificationCommand(null)));
        assertThrows(DomainValidationException.class, () -> commandService.handle(new SendEmailVerificationCommand("invalid-email")));
    }

    @Test
    @DisplayName("Should throw exception when daily limit is exceeded")
    void shouldThrowExceptionWhenDailyLimitExceeded() {
        when(sessionRepository.countRecentSessionsByEmail(eq("user@test.com"), any(Instant.class))).thenReturn(5L);

        DomainValidationException ex = assertThrows(DomainValidationException.class,
                () -> commandService.handle(new SendEmailVerificationCommand("user@test.com")));
        assertEquals("iam.error.emailVerification.dailyLimitExceeded", ex.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when cooldown is active")
    void shouldThrowExceptionWhenCooldownIsActive() {
        when(sessionRepository.countRecentSessionsByEmail(eq("user@test.com"), any(Instant.class))).thenReturn(1L);

        EmailVerificationSession existing = new EmailVerificationSession("user@test.com", "hash");
        when(sessionRepository.findLatestActiveSession("user@test.com")).thenReturn(Optional.of(existing));

        DomainValidationException ex = assertThrows(DomainValidationException.class,
                () -> commandService.handle(new SendEmailVerificationCommand("user@test.com")));
        assertEquals("iam.error.emailVerification.cooldownActive", ex.getMessage());
    }

    @Test
    @DisplayName("Should successfully verify correct OTP code")
    void shouldSuccessfullyVerifyCorrectOtpCode() {
        EmailVerificationSession session = new EmailVerificationSession("user@test.com", "hash-849201");
        when(sessionRepository.findLatestActiveSession("user@test.com")).thenReturn(Optional.of(session));
        when(otpGeneratorService.verifyOtp("849201", "hash-849201")).thenReturn(true);
        when(sessionRepository.save(any(EmailVerificationSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EmailVerificationResult result = commandService.handle(new VerifyEmailCodeCommand("user@test.com", "849201"));

        assertNotNull(result);
        assertTrue(result.verified());
        assertEquals("user@test.com", result.email());
        assertEquals(EmailVerificationStatus.VERIFIED, result.status());
        assertNotNull(result.verificationToken());
        assertNotNull(result.verifiedAt());
    }

    @Test
    @DisplayName("Should throw exception when verifying non-existent session")
    void shouldThrowExceptionWhenSessionNotFound() {
        when(sessionRepository.findLatestActiveSession("notfound@test.com")).thenReturn(Optional.empty());

        DomainValidationException ex = assertThrows(DomainValidationException.class,
                () -> commandService.handle(new VerifyEmailCodeCommand("notfound@test.com", "123456")));
        assertEquals("iam.error.emailVerification.sessionNotFound", ex.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when code format is invalid")
    void shouldThrowExceptionWhenCodeFormatIsInvalid() {
        assertThrows(DomainValidationException.class,
                () -> commandService.handle(new VerifyEmailCodeCommand("user@test.com", "12")));
        assertThrows(DomainValidationException.class,
                () -> commandService.handle(new VerifyEmailCodeCommand("user@test.com", "abcdef")));
    }
}
