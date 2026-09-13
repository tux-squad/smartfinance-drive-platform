package com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest;

import com.smartfinance.smartfinancedriveplatform.iam.application.internal.commandservices.UserCommandService;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.ForgotPasswordCommand;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.GoogleSignInCommand;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.RefreshTokenCommand;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.ResetPasswordCommand;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.SignInCommand;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.SignUpCommand;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.Password;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.Username;
import com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.resources.AuthenticatedUserResource;
import com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.resources.ForgotPasswordResource;
import com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.resources.GoogleSignInResource;
import com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.resources.RefreshTokenResource;
import com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.resources.ResetPasswordResource;
import com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.resources.SignInResource;
import com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.resources.SignUpResource;
import com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.resources.UserResource;
import com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.transform.AuthenticatedUserResourceFromEntityAssembler;
import com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.transform.SignInCommandFromResourceAssembler;
import com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.transform.SignUpCommandFromResourceAssembler;
import com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.transform.UserResourceFromEntityAssembler;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * REST Controller for authentication endpoints (Sign Up, Sign In, Refresh Token, and Password Recovery).
 */
@RestController
@RequestMapping(value = "/api/v1/auth", produces = MediaType.APPLICATION_JSON_VALUE)
public class AuthenticationController {

    private final UserCommandService userCommandService;

    public AuthenticationController(UserCommandService userCommandService) {
        this.userCommandService = userCommandService;
    }

    /**
     * Registers a new user account.
     */
    @PostMapping("/sign-up")
    public ResponseEntity<UserResource> signUp(@jakarta.validation.Valid @RequestBody SignUpResource resource) {
        SignUpCommand command = SignUpCommandFromResourceAssembler.toCommandFromResource(resource);
        var user = userCommandService.handle(command);
        if (user.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        UserResource userResource = UserResourceFromEntityAssembler.toResourceFromEntity(user.get());
        return new ResponseEntity<>(userResource, HttpStatus.CREATED);
    }

    /**
     * Authenticates a user and returns signed JWT access & refresh tokens.
     */
    @PostMapping("/sign-in")
    public ResponseEntity<AuthenticatedUserResource> signIn(@jakarta.validation.Valid @RequestBody SignInResource resource) {
        SignInCommand command = SignInCommandFromResourceAssembler.toCommandFromResource(resource);
        var authenticatedUser = userCommandService.handle(command);
        if (authenticatedUser.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        var authResult = authenticatedUser.get();
        AuthenticatedUserResource authResource = AuthenticatedUserResourceFromEntityAssembler.toResourceFromEntity(
                authResult.user(),
                authResult.token(),
                authResult.refreshToken()
        );
        return ResponseEntity.ok(authResource);
    }

    /**
     * Refreshes an expired JWT access token using a valid Refresh Token.
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<AuthenticatedUserResource> refreshToken(@jakarta.validation.Valid @RequestBody RefreshTokenResource resource) {
        RefreshTokenCommand command = new RefreshTokenCommand(resource.refreshToken());
        var authenticatedUser = userCommandService.handle(command);
        if (authenticatedUser.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        var authResult = authenticatedUser.get();
        AuthenticatedUserResource authResource = AuthenticatedUserResourceFromEntityAssembler.toResourceFromEntity(
                authResult.user(),
                authResult.token(),
                authResult.refreshToken()
        );
        return ResponseEntity.ok(authResource);
    }

    /**
     * Initiates password recovery process without exposing the raw token in response body.
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@jakarta.validation.Valid @RequestBody ForgotPasswordResource resource) {
        ForgotPasswordCommand command = new ForgotPasswordCommand(new Username(resource.username()));
        userCommandService.handle(command);
        return ResponseEntity.ok(Map.of(
                "message", "If an account with that email exists, password reset instructions have been processed."
        ));
    }

    /**
     * Resets user password using password reset token.
     */
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@jakarta.validation.Valid @RequestBody ResetPasswordResource resource) {
        ResetPasswordCommand command = new ResetPasswordCommand(
                resource.resetToken(),
                new Password(resource.newPassword())
        );
        userCommandService.handle(command);
        return ResponseEntity.ok(Map.of(
                "message", "Password reset successfully"
        ));
    }

    /**
     * Authenticates a user using Google OAuth2 ID Token and returns signed platform JWT tokens.
     */
    @PostMapping("/google")
    public ResponseEntity<AuthenticatedUserResource> googleSignIn(@jakarta.validation.Valid @RequestBody GoogleSignInResource resource) {
        GoogleSignInCommand command = new GoogleSignInCommand(resource.idToken());
        var authenticatedUser = userCommandService.handle(command);
        if (authenticatedUser.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        var authResult = authenticatedUser.get();
        AuthenticatedUserResource authResource = AuthenticatedUserResourceFromEntityAssembler.toResourceFromEntity(
                authResult.user(),
                authResult.token(),
                authResult.refreshToken()
        );
        return ResponseEntity.ok(authResource);
    }
}

