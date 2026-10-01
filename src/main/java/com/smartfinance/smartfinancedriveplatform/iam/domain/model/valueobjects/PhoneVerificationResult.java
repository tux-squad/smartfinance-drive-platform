package com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects;

import java.time.Instant;

/**
 * Result VO encapsulating the outcome of a phone verification confirmation.
 */
public record PhoneVerificationResult(
        boolean verified,
        String phoneNumber,
        PhoneVerificationStatus status,
        Instant verifiedAt,
        String verificationToken,
        String message
) {
}
