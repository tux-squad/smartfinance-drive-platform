package com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands;

import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.Password;

/**
 * Command to execute password reset using a reset token.
 */
public record ResetPasswordCommand(
        String resetToken,
        Password newPassword
) {
}
