package com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands;

/**
 * Command to request and dispatch a mobile phone verification OTP code via WhatsApp.
 */
public record SendPhoneVerificationCodeCommand(
        String phoneNumber
) {
}
