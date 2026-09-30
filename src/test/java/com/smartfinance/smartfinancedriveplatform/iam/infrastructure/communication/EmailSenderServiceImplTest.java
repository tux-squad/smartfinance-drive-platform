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

        var service = new EmailSenderServiceImpl(provider);

        assertDoesNotThrow(() -> service.sendCorporateVerificationOtp(
                "analista@viabcp.com",
                "Carlos",
                "BANCO DE CREDITO DEL PERU",
                "123456",
                10
        ));
    }
}
