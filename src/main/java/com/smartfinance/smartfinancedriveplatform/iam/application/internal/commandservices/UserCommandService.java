package com.smartfinance.smartfinancedriveplatform.iam.application.internal.commandservices;

import com.smartfinance.smartfinancedriveplatform.iam.domain.model.aggregates.User;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.RefreshTokenCommand;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.SignInCommand;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.SignUpCommand;

import java.util.Optional;

/**
 * Application command service interface for user sign-up, authentication, and token refresh operations.
 */
public interface UserCommandService {

    /**
     * Value object containing authenticated User, generated Access Token, and Refresh Token.
     */
    record AuthenticationResult(User user, String token, String refreshToken) {}

    Optional<User> handle(SignUpCommand command);

    Optional<AuthenticationResult> handle(SignInCommand command);

    Optional<AuthenticationResult> handle(RefreshTokenCommand command);
}
