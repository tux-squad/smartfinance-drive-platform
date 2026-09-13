package com.smartfinance.smartfinancedriveplatform.iam.application.internal.commandservices;

import com.smartfinance.smartfinancedriveplatform.iam.application.outboundservices.tokens.GoogleTokenVerifierService;
import com.smartfinance.smartfinancedriveplatform.iam.application.outboundservices.tokens.TokenService;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.aggregates.User;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.ForgotPasswordCommand;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.GoogleSignInCommand;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.RefreshTokenCommand;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.ResetPasswordCommand;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.RequestDealerRoleCommand;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.RequestFinancialInstitutionRoleCommand;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.SignInCommand;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.UpdateUserRoleCommand;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.SignUpCommand;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.Password;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.Roles;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.Username;
import com.smartfinance.smartfinancedriveplatform.iam.domain.repositories.UserRepository;
import com.smartfinance.smartfinancedriveplatform.iam.domain.services.HashingService;
import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service implementation for handling IAM write commands (Sign Up, Sign In, Token Refresh, Password Recovery, and Google Sign-In).
 */
@Service
public class UserCommandServiceImpl implements UserCommandService {

    private final UserRepository userRepository;
    private final HashingService hashingService;
    private final TokenService tokenService;
    private final GoogleTokenVerifierService googleTokenVerifierService;
    private final com.smartfinance.smartfinancedriveplatform.iam.infrastructure.tokens.jwt.services.TokenBlacklistService tokenBlacklistService;
    private final com.smartfinance.smartfinancedriveplatform.partners.application.outboundservices.SunatRucVerifierService sunatRucVerifierService;

    public UserCommandServiceImpl(UserRepository userRepository,
                                  HashingService hashingService,
                                  TokenService tokenService,
                                  GoogleTokenVerifierService googleTokenVerifierService,
                                  com.smartfinance.smartfinancedriveplatform.iam.infrastructure.tokens.jwt.services.TokenBlacklistService tokenBlacklistService,
                                  com.smartfinance.smartfinancedriveplatform.partners.application.outboundservices.SunatRucVerifierService sunatRucVerifierService) {
        this.userRepository = userRepository;
        this.hashingService = hashingService;
        this.tokenService = tokenService;
        this.googleTokenVerifierService = googleTokenVerifierService;
        this.tokenBlacklistService = tokenBlacklistService;
        this.sunatRucVerifierService = sunatRucVerifierService;
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
    @Transactional
    public Optional<AuthenticationResult> handle(SignInCommand command) {
        User user = userRepository.findByUsername(command.username())
                .orElseThrow(() -> new DomainValidationException("iam.error.invalidCredentials"));

        if (user.isAccountLocked()) {
            throw new DomainValidationException("iam.error.accountLocked");
        }

        if (!hashingService.matches(command.password().password(), user.getPassword().password())) {
            user.recordFailedLoginAttempt();
            userRepository.save(user);
            throw new DomainValidationException("iam.error.invalidCredentials");
        }

        if (user.getFailedLoginAttempts() > 0) {
            user.resetFailedLoginAttempts();
            userRepository.save(user);
        }

        var roleNames = user.getRoles().stream()
                .map(Enum::name)
                .toList();

        String token = tokenService.generateToken(user.getId(), user.getUsername().username(), roleNames);
        String refreshToken = tokenService.generateRefreshToken(user.getUsername().username());
        return Optional.of(new AuthenticationResult(user, token, refreshToken));
    }

    @Override
    @Transactional
    public Optional<AuthenticationResult> handle(RefreshTokenCommand command) {
        if (command.refreshToken() == null ||
            tokenBlacklistService.isBlacklisted(command.refreshToken()) ||
            !tokenService.validateRefreshToken(command.refreshToken())) {
            throw new DomainValidationException("iam.error.invalidRefreshToken");
        }

        // Invalidate old refresh token (Token Rotation)
        tokenBlacklistService.blacklistToken(command.refreshToken(), System.currentTimeMillis() + 604800000L);

        String usernameStr = tokenService.getUsernameFromToken(command.refreshToken());
        User user = userRepository.findByUsername(new Username(usernameStr))
                .orElseThrow(() -> new DomainValidationException("iam.error.userNotFound"));

        var roleNames = user.getRoles().stream()
                .map(Enum::name)
                .toList();

        String newAccessToken = tokenService.generateToken(user.getId(), user.getUsername().username(), roleNames);
        String newRefreshToken = tokenService.generateRefreshToken(user.getUsername().username());
        return Optional.of(new AuthenticationResult(user, newAccessToken, newRefreshToken));
    }

    @Override
    @Transactional
    public Optional<AuthenticationResult> handle(GoogleSignInCommand command) {
        var googleUserInfoOpt = googleTokenVerifierService.verifyToken(command.idToken());
        if (googleUserInfoOpt.isEmpty()) {
            throw new DomainValidationException("iam.error.invalidGoogleToken");
        }

        var googleUserInfo = googleUserInfoOpt.get();
        if (!googleUserInfo.emailVerified()) {
            throw new DomainValidationException("iam.error.googleEmailNotVerified");
        }

        Username username = new Username(googleUserInfo.email());

        User user = userRepository.findByUsername(username).orElseGet(() -> {
            String randomSecret = "G00gle#OAuth2_" + UUID.randomUUID().toString();
            String hashedPassword = hashingService.encode(randomSecret);
            User newUser = new User(username, new Password(hashedPassword), List.of(Roles.ROLE_USER));
            return userRepository.save(newUser);
        });

        var roleNames = user.getRoles().stream()
                .map(Enum::name)
                .toList();

        String token = tokenService.generateToken(user.getId(), user.getUsername().username(), roleNames);
        String refreshToken = tokenService.generateRefreshToken(user.getUsername().username());

        return Optional.of(new AuthenticationResult(user, token, refreshToken));
    }

    @Override
    @Transactional(readOnly = true)
    public String handle(ForgotPasswordCommand command) {
        User user = userRepository.findByUsername(command.username())
                .orElseThrow(() -> new DomainValidationException("iam.error.userNotFound"));

        return tokenService.generatePasswordResetToken(user.getUsername().username());
    }

    @Override
    @Transactional
    public boolean handle(ResetPasswordCommand command) {
        if (command.resetToken() == null || !tokenService.validateResetToken(command.resetToken())) {
            throw new DomainValidationException("iam.error.invalidResetToken");
        }

        String usernameStr = tokenService.getUsernameFromToken(command.resetToken());
        User user = userRepository.findByUsername(new Username(usernameStr))
                .orElseThrow(() -> new DomainValidationException("iam.error.userNotFound"));

        String hashedNewPassword = hashingService.encode(command.newPassword().password());
        user.setPassword(new Password(hashedNewPassword));
        userRepository.save(user);

        return true;
    }

    @Override
    @Transactional
    public Optional<User> handle(com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.UpdateUserRoleCommand command) {
        if (command.role() == Roles.ROLE_ADMIN) {
            throw new DomainValidationException("iam.error.role.adminAssignmentNotAllowed");
        }

        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new DomainValidationException("iam.error.userNotFound"));

        user.addRole(command.role());
        User updatedUser = userRepository.save(user);

        return Optional.of(updatedUser);
    }

    @Override
    @Transactional
    public Optional<User> handle(com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.RequestDealerRoleCommand command) {
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new DomainValidationException("iam.error.userNotFound"));

        var rucInfoOpt = sunatRucVerifierService.verifyRuc(command.ruc());
        if (rucInfoOpt.isEmpty()) {
            throw new DomainValidationException("iam.error.sunat.rucNotFound");
        }

        var rucInfo = rucInfoOpt.get();
        if (!rucInfo.isActiveAndHabido()) {
            throw new DomainValidationException("iam.error.sunat.rucNotActiveOrHabido");
        }

        if (!rucInfo.isAutomotiveCiiu()) {
            throw new DomainValidationException("iam.error.sunat.notAutomotiveDealer");
        }

        user.addRole(Roles.ROLE_DEALER);
        User updatedUser = userRepository.save(user);

        return Optional.of(updatedUser);
    }

    @Override
    @Transactional
    public Optional<User> handle(RequestFinancialInstitutionRoleCommand command) {
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new DomainValidationException("iam.error.userNotFound"));

        var rucInfoOpt = sunatRucVerifierService.verifyRuc(command.ruc());
        if (rucInfoOpt.isEmpty()) {
            throw new DomainValidationException("iam.error.sunat.rucNotFound");
        }

        var rucInfo = rucInfoOpt.get();
        if (!rucInfo.isActiveAndHabido()) {
            throw new DomainValidationException("iam.error.sunat.rucNotActiveOrHabido");
        }

        if (!rucInfo.isFinancialInstitutionCiiu()) {
            throw new DomainValidationException("iam.error.sunat.notFinancialInstitution");
        }

        user.addRole(Roles.ROLE_FINANCIAL_INSTITUTION);
        User updatedUser = userRepository.save(user);

        return Optional.of(updatedUser);
    }
}
