package com.smartfinance.smartfinancedriveplatform.billing.infrastructure.payment.stripe;

import com.smartfinance.smartfinancedriveplatform.billing.application.outboundservices.StripePaymentGatewayService;
import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.checkout.Session;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class StripePaymentGatewayServiceImpl implements StripePaymentGatewayService {

    private static final Logger LOGGER = LoggerFactory.getLogger(StripePaymentGatewayServiceImpl.class);

    private final String apiKey;

    public StripePaymentGatewayServiceImpl(@Value("${stripe.api-key:}") String apiKey) {
        this.apiKey = apiKey;
        if (apiKey != null && !apiKey.isBlank()) {
            Stripe.apiKey = apiKey;
        }
    }

    @Override
    public String createCustomer(String email, String name) {
        ensureStripeConfigured();
        try {
            CustomerCreateParams params = CustomerCreateParams.builder()
                    .setEmail(email)
                    .setName(name)
                    .build();
            Customer customer = Customer.create(params);
            return customer.getId();
        } catch (StripeException e) {
            LOGGER.error("Failed to create Stripe customer for email {}: {}", email, e.getMessage());
            throw new DomainValidationException("billing.error.stripeCustomerCreationFailed");
        }
    }

    @Override
    public String createCheckoutSession(String stripeCustomerId, String stripePriceId, String successUrl, String cancelUrl) {
        ensureStripeConfigured();
        try {
            SessionCreateParams params = SessionCreateParams.builder()
                    .setCustomer(stripeCustomerId)
                    .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                    .setSuccessUrl(successUrl != null ? successUrl : "https://smartfinance.drive/billing/success?session_id={CHECKOUT_SESSION_ID}")
                    .setCancelUrl(cancelUrl != null ? cancelUrl : "https://smartfinance.drive/billing/cancel")
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setPrice(stripePriceId)
                                    .setQuantity(1L)
                                    .build()
                    )
                    .build();

            Session session = Session.create(params);
            return session.getUrl();
        } catch (StripeException e) {
            LOGGER.error("Failed to create Stripe checkout session: {}", e.getMessage());
            throw new DomainValidationException("billing.error.stripeCheckoutSessionFailed");
        }
    }

    private void ensureStripeConfigured() {
        if (apiKey == null || apiKey.isBlank()) {
            LOGGER.warn("Stripe API key is not configured. Stripe operations are disabled.");
            throw new DomainValidationException("billing.error.stripeNotConfigured");
        }
        Stripe.apiKey = apiKey;
    }
}
