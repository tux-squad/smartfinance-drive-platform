package com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects;

/**
 * Value object representing verified identity claims extracted from a Firebase ID Token.
 */
public record FirebasePhoneClaims(
        String uid,
        String phoneNumber,
        boolean phoneVerified
) {
    public FirebasePhoneClaims {
        if (uid == null || uid.isBlank()) {
            throw new IllegalArgumentException("Firebase UID cannot be null or blank");
        }
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new IllegalArgumentException("Verified phone number cannot be null or blank");
        }
    }
}
