package com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * REST Request resource for confirming a phone verification code.
 */
public record VerifyPhoneCodeResource(
        @NotBlank(message = "Phone number is required")
        String phoneNumber,

        @NotBlank(message = "Verification code is required")
        @Pattern(regexp = "^\\d{6}$", message = "Verification code must be exactly 6 numeric digits")
        String code
) {
}
