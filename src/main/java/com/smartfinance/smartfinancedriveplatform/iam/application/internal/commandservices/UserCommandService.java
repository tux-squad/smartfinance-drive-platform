package com.smartfinance.smartfinancedriveplatform.iam.application.internal.commandservices;

import com.smartfinance.smartfinancedriveplatform.iam.domain.model.aggregates.User;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.SignInCommand;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.SignUpCommand;

import java.util.Optional;

/**
 * Application command service interface for user sign-up and authentication operations.
 */
public interface UserCommandService {

    /**
     * Value object containing authenticated User and generated JWT token.
     */
    record AuthenticationResult(User user, String token) {}

    Optional<User> handle(SignUpCommand command);

    Optional<AuthenticationResult> handle(SignInCommand command);
}
