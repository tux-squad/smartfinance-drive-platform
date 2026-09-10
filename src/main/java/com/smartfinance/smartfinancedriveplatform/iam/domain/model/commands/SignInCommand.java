package com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands;

import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.Password;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.Username;

/**
 * Command representing user sign-in / authentication attempt.
 */
public record SignInCommand(Username username, Password password) {
}
