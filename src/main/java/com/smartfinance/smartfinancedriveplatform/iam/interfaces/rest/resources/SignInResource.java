package com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.resources;

/**
 * REST Request resource for user sign-in authentication.
 */
public record SignInResource(
        String username,
        String password
) {
}
