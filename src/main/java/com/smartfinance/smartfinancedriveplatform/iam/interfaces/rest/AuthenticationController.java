package com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest;

import com.smartfinance.smartfinancedriveplatform.iam.application.internal.commandservices.UserCommandService;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.RefreshTokenCommand;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.SignInCommand;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.SignUpCommand;
import com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.resources.AuthenticatedUserResource;
import com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.resources.RefreshTokenResource;
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

/**
 * REST Controller for authentication endpoints (Sign Up, Sign In & Refresh Token).
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
    public ResponseEntity<UserResource> signUp(@RequestBody SignUpResource resource) {
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
    public ResponseEntity<AuthenticatedUserResource> signIn(@RequestBody SignInResource resource) {
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
    public ResponseEntity<AuthenticatedUserResource> refreshToken(@RequestBody RefreshTokenResource resource) {
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
}
