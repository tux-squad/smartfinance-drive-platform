package com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands;

import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.Password;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.Roles;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.Username;

import java.util.List;

/**
 * Command representing user sign-up registration.
 */
public record SignUpCommand(Username username, Password password, List<Roles> roles) {
}
