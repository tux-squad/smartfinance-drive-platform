package com.smartfinance.smartfinancedriveplatform.iam.application.internal.commandservices;

import com.smartfinance.smartfinancedriveplatform.iam.application.outboundservices.OtpGeneratorService;
import com.smartfinance.smartfinancedriveplatform.iam.application.outboundservices.PhoneVerificationSenderService;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.aggregates.PhoneVerificationSession;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.SendPhoneVerificationCodeCommand;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.VerifyPhoneCodeCommand;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.PhoneNumber;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.PhoneVerificationResult;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.PhoneVerificationStatus;
import com.smartfinance.smartfinancedriveplatform.iam.domain.repositories.PhoneVerificationSessionRepository;
import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PhoneVerificationCommandServiceImpl Unit Tests")
class PhoneVerificationCommandServiceImplTest {

    @Mock
    private PhoneVerificationSessionRepository sessionRepository;

    @Mock
    private OtpGeneratorService otpGeneratorService;

    @Mock
    private PhoneVerificationSenderService phoneVerificationSenderService;

    @InjectMocks
    private PhoneVerificationCommandServiceImpl commandService;

    private final String rawPhone = "+51 993913924";
    private final String normalizedPhone = "51993913924";

    @Test
    @DisplayName("Should successfully handle SendPhoneVerificationCodeCommand")
    void shouldSuccessfullySendVerificationCode() {
        when(sessionRepository.countRecentSessionsByPhoneNumber(eq(normalizedPhone), any(Instant.class))).thenReturn(0L);
        when(sessionRepository.findLatestActiveSession(normalizedPhone)).thenReturn(Optional.empty());
        when(otpGeneratorService.generateOtp()).thenReturn("123456");
        when(otpGeneratorService.hashOtp("123456")).thenReturn("hashed-otp-123456");
        when(sessionRepository.save(any(PhoneVerificationSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PhoneVerificationSession result = commandService.handle(new SendPhoneVerificationCodeCommand(rawPhone));

        assertNotNull(result);
        assertEquals(normalizedPhone, result.getPhoneNumber().fullNumber());
        assertEquals("hashed-otp-123456", result.getCodeHash());
        verify(sessionRepository).expirePendingSessions(normalizedPhone);
        verify(phoneVerificationSenderService).sendVerificationCode(normalizedPhone, "123456");
    }

    @Test
    @DisplayName("Should throw exception when resend cooldown is active")
    void shouldRejectWhenCooldownActive() {
        PhoneNumber phone = new PhoneNumber(normalizedPhone);
        PhoneVerificationSession recentSession = new PhoneVerificationSession(phone, "hash", Instant.now().minusSeconds(10), Instant.now().plus(Duration.ofMinutes(5)));

        when(sessionRepository.countRecentSessionsByPhoneNumber(eq(normalizedPhone), any(Instant.class))).thenReturn(0L);
        when(sessionRepository.findLatestActiveSession(normalizedPhone)).thenReturn(Optional.of(recentSession));

        assertThrows(DomainValidationException.class, () -> commandService.handle(new SendPhoneVerificationCodeCommand(rawPhone)));
        verify(phoneVerificationSenderService, never()).sendVerificationCode(anyString(), anyString());
    }

    @Test
    @DisplayName("Should throw exception when hourly limit is exceeded")
    void shouldRejectWhenHourlyLimitExceeded() {
        when(sessionRepository.countRecentSessionsByPhoneNumber(eq(normalizedPhone), any(Instant.class))).thenReturn(5L);

        assertThrows(DomainValidationException.class, () -> commandService.handle(new SendPhoneVerificationCodeCommand(rawPhone)));
        verify(phoneVerificationSenderService, never()).sendVerificationCode(anyString(), anyString());
    }

    @Test
    @DisplayName("Should successfully verify correct OTP code")
    void shouldSuccessfullyVerifyValidCode() {
        PhoneNumber phone = new PhoneNumber(normalizedPhone);
        PhoneVerificationSession session = new PhoneVerificationSession(phone, "valid-hash", Instant.now(), Instant.now().plus(Duration.ofMinutes(5)));

        when(sessionRepository.findLatestActiveSession(normalizedPhone)).thenReturn(Optional.of(session));
        when(otpGeneratorService.verifyOtp("123456", "valid-hash")).thenReturn(true);
        when(sessionRepository.save(any(PhoneVerificationSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PhoneVerificationResult result = commandService.handle(new VerifyPhoneCodeCommand(rawPhone, "123456"));

        assertTrue(result.verified());
        assertEquals(PhoneVerificationStatus.VERIFIED, result.status());
        assertEquals(normalizedPhone, result.phoneNumber());
    }

    @Test
    @DisplayName("Should record failed attempt on invalid code and throw exception")
    void shouldRecordFailedAttemptOnInvalidCode() {
        PhoneNumber phone = new PhoneNumber(normalizedPhone);
        PhoneVerificationSession session = new PhoneVerificationSession(phone, "valid-hash", Instant.now(), Instant.now().plus(Duration.ofMinutes(5)));

        when(sessionRepository.findLatestActiveSession(normalizedPhone)).thenReturn(Optional.of(session));
        when(otpGeneratorService.verifyOtp("999999", "valid-hash")).thenReturn(false);

        assertThrows(DomainValidationException.class, () -> commandService.handle(new VerifyPhoneCodeCommand(rawPhone, "999999")));
        assertEquals(1, session.getAttempts());
        verify(sessionRepository).save(session);
    }
}
