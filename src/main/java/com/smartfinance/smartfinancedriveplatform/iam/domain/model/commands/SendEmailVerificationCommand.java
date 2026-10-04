package com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands;

/**
 * Command to request and dispatch an email OTP verification code.
 */
public record SendEmailVerificationCommand(String email) {
}
