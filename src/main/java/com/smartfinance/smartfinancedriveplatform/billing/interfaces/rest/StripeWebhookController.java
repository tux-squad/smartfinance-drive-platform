package com.smartfinance.smartfinancedriveplatform.billing.interfaces.rest;

import com.smartfinance.smartfinancedriveplatform.billing.domain.model.valueobjects.SubscriptionStatus;
import com.smartfinance.smartfinancedriveplatform.billing.domain.repositories.InvoiceRepository;
import com.smartfinance.smartfinancedriveplatform.billing.domain.repositories.SubscriptionRepository;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for receiving and processing asynchronous Stripe Webhook events.
 */
@RestController
@RequestMapping("/api/v1/billing/webhooks/stripe")
public class StripeWebhookController {

    private static final Logger LOGGER = LoggerFactory.getLogger(StripeWebhookController.class);

    private final String webhookSecret;
    private final SubscriptionRepository subscriptionRepository;
    private final InvoiceRepository invoiceRepository;

    public StripeWebhookController(
            @Value("${stripe.webhook-secret:}") String webhookSecret,
            SubscriptionRepository subscriptionRepository,
            InvoiceRepository invoiceRepository) {
        this.webhookSecret = webhookSecret;
        this.subscriptionRepository = subscriptionRepository;
        this.invoiceRepository = invoiceRepository;
    }

    @PostMapping
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "Stripe-Signature", required = false) String sigHeader) {

        Event event;
        if (webhookSecret != null && !webhookSecret.isBlank()) {
            if (sigHeader == null) {
                LOGGER.warn("Missing Stripe-Signature header in webhook request");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing Stripe-Signature header");
            }
            try {
                event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
            } catch (SignatureVerificationException e) {
                LOGGER.error("Invalid Stripe webhook signature: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
            } catch (Exception e) {
                LOGGER.error("Error constructing Stripe webhook event: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Webhook error");
            }
        } else {
            LOGGER.warn("Stripe webhook-secret is unconfigured. Processing raw payload without signature verification.");
            try {
                event = Event.GSON.fromJson(payload, Event.class);
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid JSON payload");
            }
        }

        if (event == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Null event payload");
        }

        LOGGER.info("Received Stripe webhook event: type={}, id={}", event.getType(), event.getId());

        switch (event.getType()) {
            case "checkout.session.completed" -> handleCheckoutSessionCompleted(event);
            case "customer.subscription.deleted" -> handleSubscriptionDeleted(event);
            case "invoice.payment_failed" -> handleInvoicePaymentFailed(event);
            default -> LOGGER.debug("Unhandled Stripe webhook event type: {}", event.getType());
        }

        return ResponseEntity.ok("Event received");
    }

    private void handleCheckoutSessionCompleted(Event event) {
        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
        if (dataObjectDeserializer.getObject().isPresent()) {
            StripeObject stripeObject = dataObjectDeserializer.getObject().get();
            if (stripeObject instanceof Session session) {
                String customerId = session.getCustomer();
                String subscriptionIdStr = session.getSubscription();
                String clientReferenceId = session.getClientReferenceId(); // userId or internal subscriptionId

                LOGGER.info("Stripe Checkout completed: customer={}, stripeSubscription={}, clientRef={}",
                        customerId, subscriptionIdStr, clientReferenceId);

                if (clientReferenceId != null && !clientReferenceId.isBlank()) {
                    subscriptionRepository.findFirstByUserIdAndStatusOrderByEndDateDesc(clientReferenceId, SubscriptionStatus.ACTIVE)
                            .ifPresent(subscription -> {
                                subscription.setStripeCustomerId(customerId);
                                subscription.setStripeSubscriptionId(subscriptionIdStr);
                                subscriptionRepository.save(subscription);
                            });
                }
            }
        }
    }

    private void handleSubscriptionDeleted(Event event) {
        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
        if (dataObjectDeserializer.getObject().isPresent()) {
            StripeObject stripeObject = dataObjectDeserializer.getObject().get();
            if (stripeObject instanceof com.stripe.model.Subscription stripeSub) {
                String stripeSubId = stripeSub.getId();
                LOGGER.info("Stripe subscription deleted: {}", stripeSubId);
            }
        }
    }

    private void handleInvoicePaymentFailed(Event event) {
        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
        if (dataObjectDeserializer.getObject().isPresent()) {
            StripeObject stripeObject = dataObjectDeserializer.getObject().get();
            if (stripeObject instanceof com.stripe.model.Invoice stripeInvoice) {
                String customerId = stripeInvoice.getCustomer();
                LOGGER.warn("Stripe invoice payment failed for customer {}", customerId);
            }
        }
    }
}
