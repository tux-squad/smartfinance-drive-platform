package com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.resources;

/**
 * DTO Request resource for completing password reset.
 */
public record ResetPasswordResource(
        String resetToken,
        String newPassword
) {
}
