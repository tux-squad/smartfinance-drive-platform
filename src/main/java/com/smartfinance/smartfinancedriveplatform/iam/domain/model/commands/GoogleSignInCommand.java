package com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands;

/**
 * Command to request authentication via Google ID Token.
 */
public record GoogleSignInCommand(
        String idToken
) {
}
