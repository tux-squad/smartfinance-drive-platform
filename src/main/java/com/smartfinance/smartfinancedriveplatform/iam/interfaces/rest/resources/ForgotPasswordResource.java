package com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.resources;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO Request resource for initiating password recovery.
 */
public record ForgotPasswordResource(
        @NotBlank(message = "Username/email is required")
        @Email(message = "Username must be a valid email address")
        String username
) {
}
