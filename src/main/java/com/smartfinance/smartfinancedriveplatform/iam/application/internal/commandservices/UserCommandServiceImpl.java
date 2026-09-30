package com.smartfinance.smartfinancedriveplatform.iam.application.internal.commandservices;

import com.smartfinance.smartfinancedriveplatform.iam.application.outboundservices.EmailSenderService;
import com.smartfinance.smartfinancedriveplatform.iam.application.outboundservices.OtpGeneratorService;
import com.smartfinance.smartfinancedriveplatform.iam.application.outboundservices.tokens.GoogleTokenVerifierService;
import com.smartfinance.smartfinancedriveplatform.iam.application.outboundservices.tokens.TokenService;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.aggregates.CorporateVerificationSession;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.aggregates.User;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.ConfirmCorporateVerificationCommand;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.ForgotPasswordCommand;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.GoogleSignInCommand;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.InitiateCorporateVerificationCommand;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.RefreshTokenCommand;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.ResetPasswordCommand;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.RequestDealerRoleCommand;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.RequestFinancialInstitutionRoleCommand;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.SignInCommand;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.UpdateUserRoleCommand;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.SignUpCommand;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.CorporateVerificationInitiated;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.CorporateVerificationResult;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.CorporateVerificationSessionStatus;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.Password;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.Roles;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.Username;
import com.smartfinance.smartfinancedriveplatform.iam.domain.repositories.CorporateVerificationSessionRepository;
import com.smartfinance.smartfinancedriveplatform.iam.domain.repositories.UserRepository;
import com.smartfinance.smartfinancedriveplatform.iam.domain.services.HashingService;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.valueobjects.CorporateEntityType;
import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Service implementation for handling IAM write commands (Sign Up, Sign In, Token Refresh, Password Recovery, and Google Sign-In).
 */
@Service
public class UserCommandServiceImpl implements UserCommandService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserCommandServiceImpl.class);

    private final UserRepository userRepository;
    private final HashingService hashingService;
    private final TokenService tokenService;
    private final GoogleTokenVerifierService googleTokenVerifierService;
    private final com.smartfinance.smartfinancedriveplatform.iam.infrastructure.tokens.jwt.services.TokenBlacklistService tokenBlacklistService;
    private final com.smartfinance.smartfinancedriveplatform.partners.application.outboundservices.SunatRucVerifierService sunatRucVerifierService;
    private final com.smartfinance.smartfinancedriveplatform.partners.application.commandservices.FinancialEntityCommandService financialEntityCommandService;
    private final com.smartfinance.smartfinancedriveplatform.partners.application.commandservices.DealershipCommandService dealershipCommandService;
    private final com.smartfinance.smartfinancedriveplatform.partners.domain.services.CorporateDomainCatalog corporateDomainCatalog;
    private final CorporateVerificationSessionRepository corporateVerificationSessionRepository;
    private final OtpGeneratorService otpGeneratorService;
    private final EmailSenderService emailSenderService;
    private final com.smartfinance.smartfinancedriveplatform.partners.domain.repositories.FinancialEntityRepository financialEntityRepository;
    private final com.smartfinance.smartfinancedriveplatform.partners.domain.repositories.DealershipRepository dealershipRepository;

    @Autowired
    public UserCommandServiceImpl(UserRepository userRepository,
                                  HashingService hashingService,
                                  TokenService tokenService,
                                  GoogleTokenVerifierService googleTokenVerifierService,
                                  com.smartfinance.smartfinancedriveplatform.iam.infrastructure.tokens.jwt.services.TokenBlacklistService tokenBlacklistService,
                                  com.smartfinance.smartfinancedriveplatform.partners.application.outboundservices.SunatRucVerifierService sunatRucVerifierService,
                                  com.smartfinance.smartfinancedriveplatform.partners.application.commandservices.FinancialEntityCommandService financialEntityCommandService,
                                  com.smartfinance.smartfinancedriveplatform.partners.application.commandservices.DealershipCommandService dealershipCommandService,
                                  com.smartfinance.smartfinancedriveplatform.partners.domain.services.CorporateDomainCatalog corporateDomainCatalog,
                                  CorporateVerificationSessionRepository corporateVerificationSessionRepository,
                                  OtpGeneratorService otpGeneratorService,
                                  EmailSenderService emailSenderService,
                                  com.smartfinance.smartfinancedriveplatform.partners.domain.repositories.FinancialEntityRepository financialEntityRepository,
                                  com.smartfinance.smartfinancedriveplatform.partners.domain.repositories.DealershipRepository dealershipRepository) {
        this.userRepository = userRepository;
        this.hashingService = hashingService;
        this.tokenService = tokenService;
        this.googleTokenVerifierService = googleTokenVerifierService;
        this.tokenBlacklistService = tokenBlacklistService;
        this.sunatRucVerifierService = sunatRucVerifierService;
        this.financialEntityCommandService = financialEntityCommandService;
        this.dealershipCommandService = dealershipCommandService;
        this.corporateDomainCatalog = corporateDomainCatalog;
        this.corporateVerificationSessionRepository = corporateVerificationSessionRepository;
        this.otpGeneratorService = otpGeneratorService;
        this.emailSenderService = emailSenderService;
        this.financialEntityRepository = financialEntityRepository;
        this.dealershipRepository = dealershipRepository;
    }

    public UserCommandServiceImpl(UserRepository userRepository,
                                  HashingService hashingService,
                                  TokenService tokenService,
                                  GoogleTokenVerifierService googleTokenVerifierService,
                                  com.smartfinance.smartfinancedriveplatform.iam.infrastructure.tokens.jwt.services.TokenBlacklistService tokenBlacklistService,
                                  com.smartfinance.smartfinancedriveplatform.partners.application.outboundservices.SunatRucVerifierService sunatRucVerifierService,
                                  com.smartfinance.smartfinancedriveplatform.partners.application.commandservices.FinancialEntityCommandService financialEntityCommandService) {
        this(userRepository, hashingService, tokenService, googleTokenVerifierService, tokenBlacklistService, sunatRucVerifierService, financialEntityCommandService, null, null, null, null, null, null, null);
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

    private static final String DUMMY_HASH = "$2a$12$e0MYzXyjpJS7Pd0RVvHwHe1e8Hn4gS1wJ1B/1a1.D/1a1.D/1a1.D";

    @Override
    @Transactional
    public Optional<AuthenticationResult> handle(SignInCommand command) {
        User user = userRepository.findByUsername(command.username()).orElse(null);
        if (user == null) {
            // Perform dummy hash comparison to equalize execution time and prevent timing side-channel attack
            hashingService.matches(command.password().password(), DUMMY_HASH);
            throw new DomainValidationException("iam.error.invalidCredentials");
        }

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

        if (user.isAccountLocked()) {
            throw new DomainValidationException("iam.error.accountLocked");
        }

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

        if (user.isAccountLocked()) {
            throw new DomainValidationException("iam.error.accountLocked");
        }

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
        User user = userRepository.findByUsername(command.username()).orElse(null);
        if (user == null) {
            LOGGER.warn("Password reset requested for non-existing username: {}", maskEmail(command.username() != null ? command.username().username() : null));
            return null;
        }

        return tokenService.generatePasswordResetToken(user.getUsername().username());
    }

    @Override
    @Transactional
    public boolean handle(ResetPasswordCommand command) {
        if (command.resetToken() == null ||
            tokenBlacklistService.isBlacklisted(command.resetToken()) ||
            !tokenService.validateResetToken(command.resetToken())) {
            throw new DomainValidationException("iam.error.invalidResetToken");
        }

        String usernameStr = tokenService.getUsernameFromToken(command.resetToken());
        User user = userRepository.findByUsername(new Username(usernameStr))
                .orElseThrow(() -> new DomainValidationException("iam.error.userNotFound"));

        String hashedNewPassword = hashingService.encode(command.newPassword().password());
        user.setPassword(new Password(hashedNewPassword));
        userRepository.save(user);

        // Invalidate reset token after single use
        tokenBlacklistService.blacklistToken(command.resetToken(), System.currentTimeMillis() + 900000L);

        return true;
    }

    @Override
    @Transactional
    public Optional<User> handle(UpdateUserRoleCommand command) {
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
    public Optional<User> handle(RequestDealerRoleCommand command) {
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

        financialEntityCommandService.handle(
                new com.smartfinance.smartfinancedriveplatform.partners.domain.model.commands.LinkFinancialEntityToUserCommand(
                        user.getId().toString(),
                        rucInfo.ruc(),
                        rucInfo.razonSocial()
                )
        );

        return Optional.of(updatedUser);
    }

    @Override
    @Transactional
    public CorporateVerificationInitiated handle(InitiateCorporateVerificationCommand command) {
        if (command.userId() == null || command.userId().isBlank()) {
            throw new DomainValidationException("iam.error.userId.required");
        }
        if (command.ruc() == null || !command.ruc().matches("^\\d{11}$")) {
            throw new DomainValidationException("partners.error.dealership.ruc.invalid");
        }
        if (command.corporateEmail() == null || !command.corporateEmail().contains("@")) {
            throw new DomainValidationException("iam.error.corporateEmail.invalid");
        }

        Long userIdLong;
        try {
            userIdLong = Long.parseLong(command.userId().trim());
        } catch (NumberFormatException e) {
            throw new DomainValidationException("iam.error.userNotFound");
        }

        User user = userRepository.findById(userIdLong)
                .orElseThrow(() -> new DomainValidationException("iam.error.userNotFound"));

        // Anti-abuse check 1: Enforce maximum 5 verification requests per user per 24 hours
        Instant last24h = Instant.now().minus(24, ChronoUnit.HOURS);
        long dailyCount = corporateVerificationSessionRepository.countRecentSessionsByUserId(command.userId().trim(), last24h);
        if (dailyCount >= 5) {
            throw new DomainValidationException("iam.error.corporateVerification.dailyLimitExceeded");
        }

        // Anti-abuse check 2: Enforce a 60-second cooldown between initiation requests
        var latestOpt = corporateVerificationSessionRepository.findLatestActiveSession(command.userId().trim(), command.ruc().trim());
        if (latestOpt.isPresent()) {
            CorporateVerificationSession latest = latestOpt.get();
            if (latest.getStatus() == CorporateVerificationSessionStatus.PENDING && !latest.isExpired()) {
                long secondsElapsed = ChronoUnit.SECONDS.between(
                        latest.getExpiresAt().minus(CorporateVerificationSession.OTP_TTL), Instant.now());
                if (secondsElapsed < 60) {
                    throw new DomainValidationException("iam.error.corporateVerification.cooldownActive");
                }
            }
        }

        // Invalidate / expire any prior pending sessions for this user & RUC so old sessions cannot be hopped
        corporateVerificationSessionRepository.expirePendingSessions(command.userId().trim(), command.ruc().trim());

        // 1. Verify RUC against official SUNAT
        var rucInfoOpt = sunatRucVerifierService.verifyRuc(command.ruc().trim());
        if (rucInfoOpt.isEmpty()) {
            throw new DomainValidationException("iam.error.sunat.rucNotFound");
        }

        var rucInfo = rucInfoOpt.get();
        if (!rucInfo.isActiveAndHabido()) {
            throw new DomainValidationException("iam.error.sunat.rucNotActiveOrHabido");
        }

        // 2. Classify institution type
        CorporateEntityType entityType = corporateDomainCatalog.determineEntityType(rucInfo);
        if (entityType == CorporateEntityType.UNKNOWN) {
            throw new DomainValidationException("iam.error.sunat.notAuthorizedCorporateEntity");
        }

        // 3. Read custom domains if already registered in the DB
        Set<String> customDomains = new HashSet<>();
        if (entityType == CorporateEntityType.FINANCIAL_INSTITUTION) {
            financialEntityRepository.findByRuc(command.ruc().trim()).ifPresent(fe -> customDomains.addAll(fe.getAllowedDomains()));
        } else if (entityType == CorporateEntityType.DEALERSHIP) {
            dealershipRepository.findByRuc(command.ruc().trim()).ifPresent(d -> customDomains.addAll(d.getAllowedDomains()));
        }

        // 4. Validate domain restriction
        boolean domainAllowed = corporateDomainCatalog.isDomainAllowedForRuc(command.ruc().trim(), command.corporateEmail().trim(), customDomains);
        if (!domainAllowed) {
            throw new DomainValidationException("iam.error.corporateEmailDomainNotAllowed");
        }

        // 5. Generate secure 6-digit OTP code & hash
        String rawOtp = otpGeneratorService.generateOtp();
        String codeHash = otpGeneratorService.hashOtp(rawOtp);

        // 6. Create & persist verification session (TTL: see CorporateVerificationSession.OTP_TTL)
        Instant expiresAt = Instant.now().plus(CorporateVerificationSession.OTP_TTL);
        CorporateVerificationSession session = new CorporateVerificationSession(
                command.userId().trim(),
                command.ruc().trim(),
                command.corporateEmail().trim(),
                codeHash,
                entityType.name(),
                entityType.getTargetRole(),
                rucInfo.razonSocial(),
                rucInfo.direccion(),
                expiresAt
        );
        CorporateVerificationSession savedSession = corporateVerificationSessionRepository.save(session);

        // 7. Dispatch transactional verification email
        emailSenderService.sendCorporateVerificationOtp(
                command.corporateEmail().trim(),
                user.getUsername().username(),
                rucInfo.razonSocial(),
                rawOtp,
                (int) CorporateVerificationSession.OTP_TTL.toMinutes()
        );

        return new CorporateVerificationInitiated(
                savedSession.getId().toString(),
                true,
                maskEmail(command.corporateEmail().trim()),
                (int) CorporateVerificationSession.OTP_TTL.toSeconds()
        );
    }

    @Override
    @Transactional
    public CorporateVerificationResult handle(ConfirmCorporateVerificationCommand command) {
        if (command.userId() == null || command.userId().isBlank()) {
            throw new DomainValidationException("iam.error.userId.required");
        }
        if (command.ruc() == null || !command.ruc().matches("^\\d{11}$")) {
            throw new DomainValidationException("partners.error.dealership.ruc.invalid");
        }
        if (command.code() == null || command.code().isBlank()) {
            throw new DomainValidationException("iam.error.otpCode.required");
        }

        Long userIdLong;
        try {
            userIdLong = Long.parseLong(command.userId().trim());
        } catch (NumberFormatException e) {
            throw new DomainValidationException("iam.error.userNotFound");
        }

        User user = userRepository.findById(userIdLong)
                .orElseThrow(() -> new DomainValidationException("iam.error.userNotFound"));

        // 1. Locate latest verification session
        var sessionOpt = corporateVerificationSessionRepository.findLatestActiveSession(command.userId().trim(), command.ruc().trim());
        if (sessionOpt.isEmpty()) {
            throw new DomainValidationException("iam.error.corporateVerification.sessionExpiredOrNotFound");
        }

        CorporateVerificationSession session = sessionOpt.get();
        if (session.isBlocked()) {
            throw new DomainValidationException("iam.error.corporateVerification.sessionBlocked");
        }
        if (session.isExpired()) {
            session.markExpired();
            corporateVerificationSessionRepository.save(session);
            throw new DomainValidationException("iam.error.corporateVerification.sessionExpired");
        }
        if (session.getStatus() != CorporateVerificationSessionStatus.PENDING) {
            throw new DomainValidationException("iam.error.corporateVerification.sessionNotActive");
        }

        // 2. Validate OTP code
        boolean matches = otpGeneratorService.verifyOtp(command.code().trim(), session.getCodeHash());
        if (!matches) {
            session.recordFailedAttempt();
            corporateVerificationSessionRepository.save(session);
            if (session.isBlocked()) {
                throw new DomainValidationException("iam.error.corporateVerification.sessionBlocked");
            }
            throw new DomainValidationException("iam.error.corporateVerification.invalidOtp");
        }

        // 3. Mark session verified
        session.markVerified();
        corporateVerificationSessionRepository.save(session);

        String profileId = null;
        String profileName = session.getLegalName();

        // 4. Role assignment & Auto-profile provisioning
        if ("FINANCIAL_INSTITUTION".equalsIgnoreCase(session.getEntityType())) {
            user.addRole(Roles.ROLE_FINANCIAL_INSTITUTION);
            userRepository.save(user);

            var linkedEntity = financialEntityCommandService.handle(
                    new com.smartfinance.smartfinancedriveplatform.partners.domain.model.commands.LinkFinancialEntityToUserCommand(
                            command.userId().trim(),
                            session.getRuc(),
                            session.getLegalName()
                    )
            );
            if (linkedEntity != null && linkedEntity.getId() != null) {
                profileId = linkedEntity.getId().value().toString();
                profileName = linkedEntity.getName();
            }
        } else if ("DEALERSHIP".equalsIgnoreCase(session.getEntityType())) {
            user.addRole(Roles.ROLE_DEALER);
            userRepository.save(user);

            var linkedDealer = dealershipCommandService.handle(
                    new com.smartfinance.smartfinancedriveplatform.partners.domain.model.commands.LinkDealershipToUserCommand(
                            command.userId().trim(),
                            session.getRuc(),
                            session.getLegalName(),
                            session.getFiscalAddress(),
                            session.getCorporateEmail()
                    )
            );
            if (linkedDealer != null && linkedDealer.getId() != null) {
                profileId = linkedDealer.getId().value().toString();
                profileName = linkedDealer.getName();
            }
        }

        return new CorporateVerificationResult(
                true,
                session.getEntityType(),
                session.getTargetRole(),
                profileId,
                profileName,
                "Cuenta corporativa verificada y perfil vinculado exitosamente."
        );
    }

    private static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "***";
        }
        int atIndex = email.indexOf("@");
        String name = email.substring(0, atIndex);
        String domain = email.substring(atIndex);
        if (name.length() <= 2) {
            return name.charAt(0) + "***" + domain;
        }
        return name.charAt(0) + "***" + name.charAt(name.length() - 1) + domain;
    }
}
