package com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands;

/**
 * Command to verify a client-side Firebase Phone Authentication ID Token (JWT).
 */
public record VerifyFirebasePhoneTokenCommand(
        String firebaseIdToken,
        String callerUserId
) {
    public VerifyFirebasePhoneTokenCommand(String firebaseIdToken) {
        this(firebaseIdToken, null);
    }
}
