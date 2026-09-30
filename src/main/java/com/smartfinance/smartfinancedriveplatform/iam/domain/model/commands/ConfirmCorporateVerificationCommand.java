package com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands;

/**
 * Command to confirm B2B corporate verification with 6-digit OTP code.
 */
public record ConfirmCorporateVerificationCommand(
        String userId,
        String ruc,
        String code
) {}
