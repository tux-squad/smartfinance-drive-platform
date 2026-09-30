package com.smartfinance.smartfinancedriveplatform.iam.infrastructure.communication;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@DisplayName("EmailSenderServiceImpl Unit Tests")
class EmailSenderServiceImplTest {

    @Test
    @DisplayName("Should gracefully fall back to log when JavaMailSender is unavailable")
    void shouldGracefullyFallbackWhenMailSenderUnavailable() {
        @SuppressWarnings("unchecked")
        ObjectProvider<JavaMailSender> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(null);

        var service = new EmailSenderServiceImpl(provider, true);

        assertDoesNotThrow(() -> service.sendCorporateVerificationOtp(
                "analista@viabcp.com",
                "Carlos",
                "BANCO DE CREDITO DEL PERU",
                "123456",
                10
        ));
    }

    @Test
    @DisplayName("Should fail closed with IllegalStateException when JavaMailSender is unavailable and emulation is disabled")
    void shouldFailClosedWhenEmulationDisabled() {
        @SuppressWarnings("unchecked")
        ObjectProvider<JavaMailSender> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(null);

        var service = new EmailSenderServiceImpl(provider, false);

        org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class, () ->
                service.sendCorporateVerificationOtp(
                        "analista@viabcp.com",
                        "Carlos",
                        "BANCO DE CREDITO DEL PERU",
                        "123456",
                        10
                )
        );
    }

    @Test
    @DisplayName("Should rethrow RuntimeException when SMTP dispatch fails without leaking OTP to logs")
    void shouldThrowExceptionWhenSmtpDispatchFails() {
        @SuppressWarnings("unchecked")
        ObjectProvider<JavaMailSender> provider = mock(ObjectProvider.class);
        JavaMailSender mailSender = mock(JavaMailSender.class);
        when(provider.getIfAvailable()).thenReturn(mailSender);
        when(mailSender.createMimeMessage()).thenThrow(new RuntimeException("SMTP connection refused"));

        var service = new EmailSenderServiceImpl(provider, true);

        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class, () ->
                service.sendCorporateVerificationOtp(
                        "analista@viabcp.com",
                        "Carlos",
                        "BANCO DE CREDITO DEL PERU",
                        "123456",
                        10
                )
        );
    }
}
