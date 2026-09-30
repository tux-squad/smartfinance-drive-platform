package com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * REST request body for confirming corporate verification code.
 */
public record ConfirmCorporateVerificationResource(
        @NotBlank(message = "RUC is required")
        @Pattern(regexp = "^\\d{11}$", message = "RUC must consist of exactly 11 numeric digits")
        String ruc,

        @NotBlank(message = "Verification code is required")
        @Pattern(regexp = "^\\d{6}$", message = "Verification code must consist of exactly 6 numeric digits")
        String code
) {}
