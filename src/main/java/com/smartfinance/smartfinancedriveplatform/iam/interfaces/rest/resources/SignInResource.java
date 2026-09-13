package com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.resources;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * REST Request resource for user sign-in authentication.
 */
public record SignInResource(
        @NotBlank(message = "Username/email is required")
        @Email(message = "Username must be a valid email address")
        String username,

        @NotBlank(message = "Password is required")
        String password
) {
}
