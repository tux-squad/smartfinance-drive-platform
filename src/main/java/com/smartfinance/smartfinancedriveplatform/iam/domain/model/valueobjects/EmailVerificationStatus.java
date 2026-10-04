package com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects;

/**
 * Lifecycle status of an email OTP verification session.
 */
public enum EmailVerificationStatus {
    PENDING,
    VERIFIED,
    EXPIRED,
    BLOCKED
}
