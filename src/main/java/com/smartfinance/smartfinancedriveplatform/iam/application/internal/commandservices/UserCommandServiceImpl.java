package com.smartfinance.smartfinancedriveplatform.iam.application.internal.commandservices;

import com.smartfinance.smartfinancedriveplatform.iam.application.outboundservices.tokens.TokenService;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.aggregates.User;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.RefreshTokenCommand;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.SignInCommand;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.SignUpCommand;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.Password;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.Username;
import com.smartfinance.smartfinancedriveplatform.iam.domain.repositories.UserRepository;
import com.smartfinance.smartfinancedriveplatform.iam.domain.services.HashingService;
import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service implementation for handling IAM write commands (Sign Up, Sign In, and Token Refresh).
 */
@Service
public class UserCommandServiceImpl implements UserCommandService {

    private final UserRepository userRepository;
    private final HashingService hashingService;
    private final TokenService tokenService;

    public UserCommandServiceImpl(UserRepository userRepository,
                                  HashingService hashingService,
                                  TokenService tokenService) {
        this.userRepository = userRepository;
        this.hashingService = hashingService;
        this.tokenService = tokenService;
    }

    @Override
    @Transactional
    public Optional<User> handle(SignUpCommand command) {
        if (userRepository.existsByUsername(command.username())) {
            throw new DomainValidationException("iam.error.username.alreadyExists");
        }

        String hashedPassword = hashingService.encode(command.password().password());
        User user = new User(command.username(), new Password(hashedPassword), command.roles());
        User savedUser = userRepository.save(user);

        return Optional.of(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AuthenticationResult> handle(SignInCommand command) {
        User user = userRepository.findByUsername(command.username())
                .orElseThrow(() -> new DomainValidationException("iam.error.invalidCredentials"));

        if (!hashingService.matches(command.password().password(), user.getPassword().password())) {
            throw new DomainValidationException("iam.error.invalidCredentials");
        }

        var roleNames = user.getRoles().stream()
                .map(Enum::name)
                .toList();

        String token = tokenService.generateToken(user.getUsername().username(), roleNames);
        String refreshToken = tokenService.generateRefreshToken(user.getUsername().username());
        return Optional.of(new AuthenticationResult(user, token, refreshToken));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AuthenticationResult> handle(RefreshTokenCommand command) {
        if (command.refreshToken() == null || !tokenService.validateToken(command.refreshToken())) {
            throw new DomainValidationException("iam.error.invalidRefreshToken");
        }

        String usernameStr = tokenService.getUsernameFromToken(command.refreshToken());
        User user = userRepository.findByUsername(new Username(usernameStr))
                .orElseThrow(() -> new DomainValidationException("iam.error.userNotFound"));

        var roleNames = user.getRoles().stream()
                .map(Enum::name)
                .toList();

        String newAccessToken = tokenService.generateToken(user.getUsername().username(), roleNames);
        String newRefreshToken = tokenService.generateRefreshToken(user.getUsername().username());
        return Optional.of(new AuthenticationResult(user, newAccessToken, newRefreshToken));
    }
}
