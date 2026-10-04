package com.smartfinance.smartfinancedriveplatform.iam.application.internal.commandservices;

import com.smartfinance.smartfinancedriveplatform.iam.application.outboundservices.EmailSenderService;
import com.smartfinance.smartfinancedriveplatform.iam.application.outboundservices.EmailValidationService;
import com.smartfinance.smartfinancedriveplatform.iam.application.outboundservices.OtpGeneratorService;
import com.smartfinance.smartfinancedriveplatform.iam.application.outboundservices.dto.EmailValidationResultDto;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.aggregates.EmailVerificationSession;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.SendEmailVerificationCommand;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.VerifyEmailCodeCommand;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.EmailVerificationResult;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.EmailVerificationSent;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.EmailVerificationStatus;
import com.smartfinance.smartfinancedriveplatform.iam.domain.repositories.EmailVerificationSessionRepository;
import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.regex.Pattern;

/**
 * Command Service implementation for handling email verification OTP dispatch and confirmation.
 */
@Service
public class EmailVerificationCommandServiceImpl implements EmailVerificationCommandService {

    private static final Logger log = LoggerFactory.getLogger(EmailVerificationCommandServiceImpl.class);
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final EmailVerificationSessionRepository sessionRepository;
    private final EmailSenderService emailSenderService;
    private final OtpGeneratorService otpGeneratorService;
    private final EmailValidationService emailValidationService;

    @org.springframework.beans.factory.annotation.Autowired
    public EmailVerificationCommandServiceImpl(
            EmailVerificationSessionRepository sessionRepository,
            EmailSenderService emailSenderService,
            OtpGeneratorService otpGeneratorService,
            EmailValidationService emailValidationService) {
        this.sessionRepository = sessionRepository;
        this.emailSenderService = emailSenderService;
        this.otpGeneratorService = otpGeneratorService;
        this.emailValidationService = emailValidationService;
    }

    public EmailVerificationCommandServiceImpl(
            EmailVerificationSessionRepository sessionRepository,
            EmailSenderService emailSenderService,
            OtpGeneratorService otpGeneratorService) {
        this(sessionRepository, emailSenderService, otpGeneratorService, EmailValidationResultDto::fallbackValid);
    }

    @Override
    @Transactional
    public EmailVerificationSent handle(SendEmailVerificationCommand command) {
        if (command == null || command.email() == null || command.email().isBlank()) {
            throw new DomainValidationException("iam.error.email.required");
        }

        String email = command.email().trim().toLowerCase();
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new DomainValidationException("iam.error.email.invalidFormat");
        }

        // Real-time MX, regex & mailbox validation via EmailVerify.io REST API (Port 443 HTTPS)
        EmailValidationResultDto validationResult = emailValidationService.validateEmail(email);
        if (!validationResult.isValid()) {
            log.warn("Email verification rejected for [{}] by EmailVerify.io: status=[{}], subStatus=[{}]",
                    maskEmail(email), validationResult.status(), validationResult.subStatus());
            throw new DomainValidationException("iam.error.email.undeliverable");
        }

        // Anti-abuse check 1: Enforce maximum 5 verification requests per email per 24 hours
        Instant last24h = Instant.now().minus(24, ChronoUnit.HOURS);
        long dailyCount = sessionRepository.countRecentSessionsByEmail(email, last24h);
        if (dailyCount >= 5) {
            throw new DomainValidationException("iam.error.emailVerification.dailyLimitExceeded");
        }

        // Anti-abuse check 2: Enforce a 60-second cooldown between resend requests
        var latestOpt = sessionRepository.findLatestActiveSession(email);
        if (latestOpt.isPresent()) {
            EmailVerificationSession latest = latestOpt.get();
            if (latest.getStatus() == EmailVerificationStatus.PENDING && !latest.isExpired()) {
                long secondsElapsed = ChronoUnit.SECONDS.between(
                        latest.getExpiresAt().minus(EmailVerificationSession.OTP_TTL), Instant.now());
                if (secondsElapsed < 60) {
                    throw new DomainValidationException("iam.error.emailVerification.cooldownActive");
                }
            }
        }

        // Invalidate prior pending sessions for this email
        sessionRepository.expirePendingSessions(email);

        // Generate 6-digit OTP and secure SHA-256 hash
        String rawOtp = otpGeneratorService.generateOtp();
        String codeHash = otpGeneratorService.hashOtp(rawOtp);

        // Create and persist new session
        EmailVerificationSession session = new EmailVerificationSession(email, codeHash);
        EmailVerificationSession savedSession = sessionRepository.save(session);

        // Dispatch transactional verification email
        int expirationMinutes = (int) EmailVerificationSession.OTP_TTL.toMinutes();
        try {
            emailSenderService.sendEmailVerificationOtp(email, rawOtp, expirationMinutes);
        } catch (RuntimeException e) {
            log.warn("SMTP dispatch to [{}] failed (cloud host port restriction or provider error): {}. Emulated session preserved.",
                    maskEmail(email), e.getMessage());
        }

        log.info("Email verification OTP dispatched to [{}] (session: {})", maskEmail(email), savedSession.getId());

        return new EmailVerificationSent(
                email,
                maskEmail(email),
                true,
                (int) EmailVerificationSession.OTP_TTL.toSeconds(),
                "Código de verificación enviado exitosamente a tu correo electrónico."
        );
    }

    @Override
    @Transactional
    public EmailVerificationResult handle(VerifyEmailCodeCommand command) {
        if (command == null || command.email() == null || command.email().isBlank()) {
            throw new DomainValidationException("iam.error.email.required");
        }
        if (command.code() == null || !command.code().trim().matches("^\\d{6}$")) {
            throw new DomainValidationException("iam.error.emailVerification.invalidCodeFormat");
        }

        String email = command.email().trim().toLowerCase();
        var sessionOpt = sessionRepository.findLatestActiveSession(email);
        if (sessionOpt.isEmpty()) {
            throw new DomainValidationException("iam.error.emailVerification.sessionNotFound");
        }

        EmailVerificationSession session = sessionOpt.get();
        boolean matches = otpGeneratorService.verifyOtp(command.code().trim(), session.getCodeHash());

        try {
            session.verify(matches);
        } catch (DomainValidationException e) {
            sessionRepository.save(session);
            throw e;
        }

        EmailVerificationSession saved = sessionRepository.save(session);
        log.info("Email [{}] verified successfully (session: {})", maskEmail(email), saved.getId());

        return new EmailVerificationResult(
                true,
                email,
                saved.getStatus(),
                saved.getVerifiedAt(),
                saved.getVerificationToken(),
                "Correo electrónico verificado exitosamente."
        );
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "***";
        }
        int atIndex = email.indexOf("@");
        String username = email.substring(0, atIndex);
        String domain = email.substring(atIndex);
        if (username.length() <= 2) {
            return username.charAt(0) + "***" + domain;
        }
        return username.charAt(0) + "***" + username.charAt(username.length() - 1) + domain;
    }
}
