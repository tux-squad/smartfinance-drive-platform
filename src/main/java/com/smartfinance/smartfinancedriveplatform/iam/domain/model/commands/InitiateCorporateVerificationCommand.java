package com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands;

/**
 * Command to initiate B2B corporate verification via SUNAT lookup and email OTP.
 */
public record InitiateCorporateVerificationCommand(
        String userId,
        String ruc,
        String corporateEmail
) {}
