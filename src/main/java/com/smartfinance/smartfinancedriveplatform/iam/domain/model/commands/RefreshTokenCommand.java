package com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands;

/**
 * Command to request JWT access token refresh.
 */
public record RefreshTokenCommand(
        String refreshToken
) {
}
