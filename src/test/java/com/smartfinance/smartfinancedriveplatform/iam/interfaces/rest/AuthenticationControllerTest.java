package com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest;

import com.smartfinance.smartfinancedriveplatform.iam.application.internal.commandservices.UserCommandService;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.aggregates.User;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.GoogleSignInCommand;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.RefreshTokenCommand;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.SignInCommand;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.SignUpCommand;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.Password;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.Username;
import com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.resources.AuthenticatedUserResource;
import com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.resources.GoogleSignInResource;
import com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.resources.RefreshTokenResource;
import com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.resources.SignInResource;
import com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.resources.SignUpResource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthenticationController Unit Tests")
class AuthenticationControllerTest {

    @Mock
    private UserCommandService userCommandService;

    @InjectMocks
    private AuthenticationController authenticationController;

    @Test
    @DisplayName("Should return 201 Created on successful sign up")
    void shouldReturnCreatedOnSignUp() {
        SignUpResource resource = new SignUpResource("user@example.com", "Password123!", List.of("ROLE_USER"));
        User user = new User(1L, new Username("user@example.com"), new Password("$2a$10$hashed1Password123"), List.of());

        when(userCommandService.handle(any(SignUpCommand.class))).thenReturn(Optional.of(user));

        ResponseEntity<?> response = authenticationController.signUp(resource);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("Should return 200 OK on successful sign in")
    void shouldReturnOkOnSignIn() {
        SignInResource resource = new SignInResource("user@example.com", "Password123!");
        User user = new User(1L, new Username("user@example.com"), new Password("$2a$10$hashed1Password123"), List.of());
        UserCommandService.AuthenticationResult authResult =
                new UserCommandService.AuthenticationResult(user, "mocked-jwt-token", "mocked-refresh-token");

        when(userCommandService.handle(any(SignInCommand.class))).thenReturn(Optional.of(authResult));

        ResponseEntity<AuthenticatedUserResource> response = authenticationController.signIn(resource);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("mocked-jwt-token", response.getBody().token());
        assertEquals("mocked-refresh-token", response.getBody().refreshToken());
    }

    @Test
    @DisplayName("Should return 200 OK on successful refresh token")
    void shouldReturnOkOnRefreshToken() {
        RefreshTokenResource resource = new RefreshTokenResource("valid-refresh-token");
        User user = new User(1L, new Username("user@example.com"), new Password("$2a$10$hashed1Password123"), List.of());
        UserCommandService.AuthenticationResult authResult =
                new UserCommandService.AuthenticationResult(user, "new-access-token", "new-refresh-token");

        when(userCommandService.handle(any(RefreshTokenCommand.class))).thenReturn(Optional.of(authResult));

        ResponseEntity<AuthenticatedUserResource> response = authenticationController.refreshToken(resource);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("new-access-token", response.getBody().token());
        assertEquals("new-refresh-token", response.getBody().refreshToken());
    }

    @Test
    @DisplayName("Should return 200 OK on successful google sign in")
    void shouldReturnOkOnGoogleSignIn() {
        GoogleSignInResource resource = new GoogleSignInResource("valid-google-id-token");
        User user = new User(1L, new Username("user@gmail.com"), new Password("$2a$10$hashed1Password123"), List.of());
        UserCommandService.AuthenticationResult authResult =
                new UserCommandService.AuthenticationResult(user, "google-access-token", "google-refresh-token");

        when(userCommandService.handle(any(GoogleSignInCommand.class))).thenReturn(Optional.of(authResult));

        ResponseEntity<AuthenticatedUserResource> response = authenticationController.googleSignIn(resource);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("google-access-token", response.getBody().token());
        assertEquals("google-refresh-token", response.getBody().refreshToken());
    }
}

