package com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.resources;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * REST request body for initiating corporate verification.
 */
public record InitiateCorporateVerificationResource(
        @NotBlank(message = "RUC is required")
        @Pattern(regexp = "^\\d{11}$", message = "RUC must consist of exactly 11 numeric digits")
        String ruc,

        @NotBlank(message = "Corporate email is required")
        @Email(message = "Invalid corporate email address")
        String corporateEmail
) {}
