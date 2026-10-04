package com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects;

import java.time.Instant;

/**
 * Value Object representing the result of an email verification confirmation.
 */
public record EmailVerificationResult(
        boolean verified,
        String email,
        EmailVerificationStatus status,
        Instant verifiedAt,
        String verificationToken,
        String message
) {
}
