package com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.resources;

/**
 * REST response resource representation for completed corporate verification.
 */
public record CorporateVerificationResultResource(
        boolean verified,
        String entityType,
        String assignedRole,
        String profileId,
        String profileName,
        String message
) {}
