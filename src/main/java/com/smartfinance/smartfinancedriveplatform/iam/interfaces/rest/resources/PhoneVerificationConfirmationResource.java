package com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.resources;

import java.time.Instant;

/**
 * REST Response resource representing a completed phone verification confirmation.
 */
public record PhoneVerificationConfirmationResource(
        boolean verified,
        String phoneNumber,
        String status,
        Instant verifiedAt,
        String verificationToken,
        String message
) {
}
