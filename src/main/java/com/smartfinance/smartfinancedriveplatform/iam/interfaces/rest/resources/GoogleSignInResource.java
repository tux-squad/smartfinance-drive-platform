package com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.resources;

/**
 * REST Request payload resource for Google Sign-In authentication.
 */
public record GoogleSignInResource(
        String idToken
) {
}
