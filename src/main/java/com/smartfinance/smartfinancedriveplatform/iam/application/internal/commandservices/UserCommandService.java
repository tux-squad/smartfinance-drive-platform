package com.smartfinance.smartfinancedriveplatform.iam.application.internal.commandservices;

import com.smartfinance.smartfinancedriveplatform.iam.domain.model.aggregates.User;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.ForgotPasswordCommand;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.GoogleSignInCommand;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.RefreshTokenCommand;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.ResetPasswordCommand;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.SignInCommand;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.SignUpCommand;

import java.util.Optional;

/**
 * Application command service interface for user sign-up, authentication, token refresh, and password recovery.
 */
public interface UserCommandService {

    /**
     * Value object containing authenticated User, generated Access Token, and Refresh Token.
     */
    record AuthenticationResult(User user, String token, String refreshToken) {}

    Optional<User> handle(SignUpCommand command);

    Optional<AuthenticationResult> handle(SignInCommand command);

    Optional<AuthenticationResult> handle(RefreshTokenCommand command);

    Optional<AuthenticationResult> handle(GoogleSignInCommand command);

    String handle(ForgotPasswordCommand command);

    boolean handle(ResetPasswordCommand command);
}
