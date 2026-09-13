package com.smartfinance.smartfinancedriveplatform.billing.interfaces.rest.resources;

/**
 * Resource DTO representing a successful Stripe Checkout Session creation response.
 */
public record CheckoutSessionResponseResource(
        String checkoutUrl
) {}
