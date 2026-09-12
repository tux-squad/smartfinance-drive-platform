package com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.resources;

/**
 * REST Request payload resource for token refresh.
 */
public record RefreshTokenResource(
        String refreshToken
) {
}
