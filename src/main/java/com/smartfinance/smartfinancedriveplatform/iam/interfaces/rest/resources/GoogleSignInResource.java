package com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;

/**
 * REST Request payload resource for Google Sign-In authentication.
 */
public record GoogleSignInResource(
        @NotBlank(message = "Google ID token is required")
        String idToken
) {
}
