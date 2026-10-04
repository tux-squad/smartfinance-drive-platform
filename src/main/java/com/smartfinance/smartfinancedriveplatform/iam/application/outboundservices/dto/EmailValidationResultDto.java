package com.smartfinance.smartfinancedriveplatform.iam.application.outboundservices.dto;

/**
 * Data Transfer Object representing the result of a real-time email verification check.
 *
 * @param email     The validated email address.
 * @param status    The primary verification status ("valid", "invalid", etc.).
 * @param subStatus The detailed sub-status ("permitted", "mailbox_not_found", etc.).
 * @param isValid   True if the email address is confirmed active and deliverable.
 */
public record EmailValidationResultDto(
        String email,
        String status,
        String subStatus,
        boolean isValid
) {
    public static EmailValidationResultDto valid(String email, String status, String subStatus) {
        boolean valid = "valid".equalsIgnoreCase(status) || "permitted".equalsIgnoreCase(subStatus);
        return new EmailValidationResultDto(email, status, subStatus, valid);
    }

    public static EmailValidationResultDto fallbackValid(String email) {
        return new EmailValidationResultDto(email, "unknown", "bypassed", true);
    }
}
