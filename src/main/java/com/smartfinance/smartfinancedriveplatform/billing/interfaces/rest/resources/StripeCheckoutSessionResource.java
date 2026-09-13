package com.smartfinance.smartfinancedriveplatform.billing.interfaces.rest.resources;

/**
 * Resource DTO containing the Stripe Checkout Session URL for client redirection.
 */
public record StripeCheckoutSessionResource(
        String checkoutUrl
) {}
