package com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.resources;

/**
 * REST response resource representation for initiated corporate verification.
 */
public record CorporateVerificationInitiatedResource(
        String sessionId,
        boolean sessionActive,
        String maskedEmail,
        int expiresInSeconds,
        String message
) {}
