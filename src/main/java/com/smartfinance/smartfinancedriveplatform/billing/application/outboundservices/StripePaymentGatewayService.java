package com.smartfinance.smartfinancedriveplatform.billing.application.outboundservices;

/**
 * Outbound Port interface for interacting with Stripe Payment Gateway APIs.
 */
public interface StripePaymentGatewayService {
    /**
     * Creates a new customer in Stripe.
     */
    String createCustomer(String email, String name);

    /**
     * Creates a Stripe Checkout Session for subscription payment.
     */
    String createCheckoutSession(String stripeCustomerId, String stripePriceId, String successUrl, String cancelUrl);
}
