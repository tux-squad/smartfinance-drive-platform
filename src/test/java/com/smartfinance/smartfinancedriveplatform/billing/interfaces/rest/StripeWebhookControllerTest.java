package com.smartfinance.smartfinancedriveplatform.billing.interfaces.rest;

import com.smartfinance.smartfinancedriveplatform.billing.domain.repositories.InvoiceRepository;
import com.smartfinance.smartfinancedriveplatform.billing.domain.repositories.SubscriptionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StripeWebhookController Unit Tests")
class StripeWebhookControllerTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    @Test
    @DisplayName("Should return 400 Bad Request on missing signature when secret is set")
    void shouldReturnBadRequestOnMissingSignature() {
        StripeWebhookController controller = new StripeWebhookController("whsec_secret", subscriptionRepository, invoiceRepository);

        ResponseEntity<String> response = controller.handleStripeWebhook("{}", null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Missing Stripe-Signature header", response.getBody());
    }

    @Test
    @DisplayName("Should return 400 Bad Request when webhook secret is unconfigured")
    void shouldReturnBadRequestWhenSecretUnconfigured() {
        StripeWebhookController controller = new StripeWebhookController("", subscriptionRepository, invoiceRepository);

        String sampleEventJson = """
                {
                  "id": "evt_test_123",
                  "type": "payment_intent.succeeded"
                }
                """;

        ResponseEntity<String> response = controller.handleStripeWebhook(sampleEventJson, null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Webhook secret is not configured", response.getBody());
    }
}
