package com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects;

/**
 * Value Object representing the initiated state of corporate verification.
 */
public record CorporateVerificationInitiated(
        String sessionId,
        boolean sessionActive,
        String maskedEmail,
        int expiresInSeconds
) {}
