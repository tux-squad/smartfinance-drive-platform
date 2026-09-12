package com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.resources;

/**
 * DTO Request resource for initiating password recovery.
 */
public record ForgotPasswordResource(
        String username
) {
}
