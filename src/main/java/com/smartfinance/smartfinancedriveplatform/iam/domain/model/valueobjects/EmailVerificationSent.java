package com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects;

/**
 * Value Object representing the response of a dispatched email OTP code.
 */
public record EmailVerificationSent(
        String email,
        String maskedEmail,
        boolean sessionActive,
        int expiresInSeconds,
        String message
) {
}
