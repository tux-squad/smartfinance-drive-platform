package com.smartfinance.smartfinancedriveplatform.billing.infrastructure.payment.stripe;

import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("StripePaymentGatewayServiceImpl Unit Tests")
class StripePaymentGatewayServiceImplTest {

    @Test
    @DisplayName("Should throw exception when Stripe API key is unconfigured")
    void shouldThrowExceptionWhenStripeNotConfigured() {
        StripePaymentGatewayServiceImpl service = new StripePaymentGatewayServiceImpl("", "http://localhost:5173/success", "http://localhost:5173/cancel");

        assertThrows(DomainValidationException.class, () ->
                service.createCustomer("user@example.com", "Test User"));

        assertThrows(DomainValidationException.class, () ->
                service.createCheckoutSession("cus_123", "price_123", "usr_123", "http://localhost:5173/success", "http://localhost:5173/cancel"));
    }

    @Test
    @DisplayName("Should reject untrusted redirect URLs to prevent open redirect and phishing")
    void shouldRejectUntrustedRedirectUrls() {
        StripePaymentGatewayServiceImpl service = new StripePaymentGatewayServiceImpl(
                "sk_test_mock",
                "http://localhost:5173/success",
                "http://localhost:5173/cancel",
                "http://localhost:5173,http://localhost:3000"
        );

        // Untrusted success URL
        DomainValidationException ex1 = assertThrows(DomainValidationException.class, () ->
                service.createCheckoutSession("cus_123", "price_123", "usr_123", "https://attacker.com/phishing", "http://localhost:5173/cancel"));
        assertTrue(ex1.getMessage().contains("untrustedRedirectUrl"));

        // Untrusted cancel URL
        DomainValidationException ex2 = assertThrows(DomainValidationException.class, () ->
                service.createCheckoutSession("cus_123", "price_123", "usr_123", "http://localhost:5173/success", "javascript:alert(1)"));
        assertTrue(ex2.getMessage().contains("untrustedRedirectUrl") || ex2.getMessage().contains("invalidRedirectUrl"));
    }
}
