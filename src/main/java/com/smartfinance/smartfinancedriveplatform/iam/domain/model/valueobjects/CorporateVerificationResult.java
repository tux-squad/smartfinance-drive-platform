package com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects;

/**
 * Value Object representing the completed state of corporate verification.
 */
public record CorporateVerificationResult(
        boolean verified,
        String entityType,
        String assignedRole,
        String profileId,
        String profileName,
        String message
) {}
