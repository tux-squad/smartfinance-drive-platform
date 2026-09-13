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
        StripePaymentGatewayServiceImpl service = new StripePaymentGatewayServiceImpl("", "http://success", "http://cancel");

        assertThrows(DomainValidationException.class, () ->
                service.createCustomer("user@example.com", "Test User"));

        assertThrows(DomainValidationException.class, () ->
                service.createCheckoutSession("cus_123", "price_123", "usr_123", "http://success", "http://cancel"));
    }
}
