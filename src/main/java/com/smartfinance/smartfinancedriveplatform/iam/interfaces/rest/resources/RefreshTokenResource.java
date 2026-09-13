package com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;

/**
 * REST Request payload resource for token refresh.
 */
public record RefreshTokenResource(
        @NotBlank(message = "Refresh token is required")
        String refreshToken
) {
}
