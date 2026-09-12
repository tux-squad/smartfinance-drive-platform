package com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands;

import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.Username;

/**
 * Command to request password recovery token.
 */
public record ForgotPasswordCommand(
        Username username
) {
}
