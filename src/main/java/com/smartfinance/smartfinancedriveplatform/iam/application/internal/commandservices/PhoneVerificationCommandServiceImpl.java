package com.smartfinance.smartfinancedriveplatform.iam.application.internal.commandservices;

import com.smartfinance.smartfinancedriveplatform.iam.application.outboundservices.OtpGeneratorService;
import com.smartfinance.smartfinancedriveplatform.iam.application.outboundservices.PhoneVerificationSenderService;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.aggregates.PhoneVerificationSession;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.SendPhoneVerificationCodeCommand;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.VerifyPhoneCodeCommand;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.PhoneNumber;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.PhoneVerificationResult;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.PhoneVerificationStatus;
import com.smartfinance.smartfinancedriveplatform.iam.domain.repositories.PhoneVerificationSessionRepository;
import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

/**
 * Service implementation for handling phone verification workflows (OTP generation, WhatsApp dispatch, verification confirmation).
 */
@Service
public class PhoneVerificationCommandServiceImpl implements PhoneVerificationCommandService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PhoneVerificationCommandServiceImpl.class);
    private static final int MAX_HOURLY_DISPATCHES = 5;

    private final PhoneVerificationSessionRepository sessionRepository;
    private final OtpGeneratorService otpGeneratorService;
    private final PhoneVerificationSenderService phoneVerificationSenderService;
    private final int otpTtlMinutes;

    @Autowired
    public PhoneVerificationCommandServiceImpl(
            PhoneVerificationSessionRepository sessionRepository,
            OtpGeneratorService otpGeneratorService,
            PhoneVerificationSenderService phoneVerificationSenderService,
            @Value("${factiliza.whatsapp.otp-ttl-minutes:5}") int otpTtlMinutes) {
        this.sessionRepository = sessionRepository;
        this.otpGeneratorService = otpGeneratorService;
        this.phoneVerificationSenderService = phoneVerificationSenderService;
        this.otpTtlMinutes = otpTtlMinutes > 0 ? otpTtlMinutes : 5;
    }

    public PhoneVerificationCommandServiceImpl(
            PhoneVerificationSessionRepository sessionRepository,
            OtpGeneratorService otpGeneratorService,
            PhoneVerificationSenderService phoneVerificationSenderService) {
        this(sessionRepository, otpGeneratorService, phoneVerificationSenderService, 5);
    }

    @Override
    @Transactional
    public PhoneVerificationSession handle(SendPhoneVerificationCodeCommand command) {
        PhoneNumber phone = new PhoneNumber(command.phoneNumber());
        Instant now = Instant.now();

        // 1. Periodic cleanup of expired sessions older than 7 days
        sessionRepository.deleteExpiredSessionsBefore(now.minus(Duration.ofDays(7)));

        // 2. Check rate-limit / hourly limit
        long recentSessions = sessionRepository.countRecentSessionsByPhoneNumber(
                phone.fullNumber(), now.minus(Duration.ofHours(1)));
        if (recentSessions >= MAX_HOURLY_DISPATCHES) {
            LOGGER.warn("Hourly phone verification dispatch limit exceeded for recipient [{}]", phone.getMasked());
            throw new DomainValidationException("iam.error.phoneVerification.hourlyLimitExceeded");
        }

        // 3. Check cooldown with latest session
        var latestOpt = sessionRepository.findLatestActiveSession(phone.fullNumber());
        if (latestOpt.isPresent()) {
            PhoneVerificationSession latest = latestOpt.get();
            if (latest.getStatus() == PhoneVerificationStatus.PENDING && !latest.canResend(now)) {
                LOGGER.warn("Resend cooldown active for recipient [{}]", phone.getMasked());
                throw new DomainValidationException("iam.error.phoneVerification.cooldownActive");
            }
        }

        // 4. Invalidate previous pending sessions
        sessionRepository.expirePendingSessions(phone.fullNumber());

        // 5. Generate new OTP and SHA-256 hash
        String rawOtp = otpGeneratorService.generateOtp();
        String codeHash = otpGeneratorService.hashOtp(rawOtp);

        PhoneVerificationSession newSession = new PhoneVerificationSession(
                command.userId(),
                phone,
                codeHash,
                now,
                now.plus(Duration.ofMinutes(otpTtlMinutes))
        );

        PhoneVerificationSession savedSession = sessionRepository.save(newSession);

        // 6. Dispatch via WhatsApp outbound service (safe logging internally)
        phoneVerificationSenderService.sendVerificationCode(phone.fullNumber(), rawOtp);

        LOGGER.info("Phone verification session [{}] initiated for recipient [{}]", savedSession.getId(), phone.getMasked());
        return savedSession;
    }

    @Override
    @Transactional
    public PhoneVerificationResult handle(VerifyPhoneCodeCommand command) {
        PhoneNumber phone = new PhoneNumber(command.phoneNumber());

        var sessionOpt = sessionRepository.findLatestActiveSession(phone.fullNumber());
        if (sessionOpt.isEmpty()) {
            LOGGER.warn("Verification attempted for non-existent session: recipient [{}]", phone.getMasked());
            throw new DomainValidationException("iam.error.phoneVerification.sessionNotFound");
        }

        PhoneVerificationSession session = sessionOpt.get();

        if (session.isBlocked()) {
            LOGGER.warn("Verification attempted on blocked session for recipient [{}]", phone.getMasked());
            throw new DomainValidationException("iam.error.phoneVerification.sessionBlocked");
        }

        if (session.isExpired() || session.getStatus() == PhoneVerificationStatus.EXPIRED) {
            LOGGER.warn("Verification attempted on expired session for recipient [{}]", phone.getMasked());
            throw new DomainValidationException("iam.error.phoneVerification.sessionExpired");
        }

        if (session.getStatus() == PhoneVerificationStatus.VERIFIED) {
            return new PhoneVerificationResult(true, phone.fullNumber(), PhoneVerificationStatus.VERIFIED, session.getVerifiedAt(), session.getId().toString(), "Phone number is already verified");
        }

        boolean isValidCode = otpGeneratorService.verifyOtp(command.code(), session.getCodeHash());
        if (!isValidCode) {
            session.recordFailedAttempt();
            sessionRepository.save(session);
            LOGGER.warn("Invalid OTP code attempt ({}/{}) for recipient [{}]", session.getAttempts(), PhoneVerificationSession.MAX_ATTEMPTS, phone.getMasked());
            if (session.isBlocked()) {
                throw new DomainValidationException("iam.error.phoneVerification.sessionBlocked");
            }
            throw new DomainValidationException("iam.error.phoneVerification.invalidCode");
        }

        session.markVerified();
        PhoneVerificationSession verifiedSession = sessionRepository.save(session);

        LOGGER.info("Phone number [{}] successfully verified via session [{}]", phone.getMasked(), verifiedSession.getId());
        return new PhoneVerificationResult(true, phone.fullNumber(), PhoneVerificationStatus.VERIFIED, verifiedSession.getVerifiedAt(), verifiedSession.getId().toString(), "Phone number successfully verified");
    }
}
