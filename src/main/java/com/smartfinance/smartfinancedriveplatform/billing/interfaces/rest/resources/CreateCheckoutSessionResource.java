package com.smartfinance.smartfinancedriveplatform.billing.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Resource DTO representing a request to initiate a Stripe Checkout Session.
 */
public record CreateCheckoutSessionResource(
        @NotNull @NotBlank String stripePriceId,
        String successUrl,
        String cancelUrl
) {}
