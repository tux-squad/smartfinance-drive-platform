package com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands;

/**
 * Command to verify a previously dispatched mobile phone OTP code.
 */
public record VerifyPhoneCodeCommand(
        String phoneNumber,
        String code,
        String callerUserId
) {
    public VerifyPhoneCodeCommand(String phoneNumber, String code) {
        this(phoneNumber, code, null);
    }
}
