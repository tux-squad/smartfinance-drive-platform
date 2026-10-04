package com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands;

/**
 * Command to verify an input email OTP code.
 */
public record VerifyEmailCodeCommand(String email, String code) {
}
