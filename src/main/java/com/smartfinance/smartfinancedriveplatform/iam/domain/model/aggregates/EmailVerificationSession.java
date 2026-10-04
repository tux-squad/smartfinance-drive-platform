package com.smartfinance.smartfinancedriveplatform.iam.domain.model.aggregates;

import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.EmailVerificationStatus;
import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * Aggregate Root representing an email OTP verification session.
 */
@Getter
public class EmailVerificationSession extends AbstractDomainAggregateRoot<EmailVerificationSession> {

    public static final int MAX_ATTEMPTS = 3;
    public static final Duration OTP_TTL = Duration.ofMinutes(10);
    public static final Duration RESEND_COOLDOWN = Duration.ofSeconds(60);

    private final UUID id;
    private final String email;
    private final String codeHash;
    private int attempts;
    private EmailVerificationStatus status;
    private String verificationToken;
    private final Instant createdAt;
    private final Instant expiresAt;
    private Instant verifiedAt;

    /**
     * Creation constructor for a new email verification session.
     */
    public EmailVerificationSession(String email, String codeHash) {
        if (email == null || email.isBlank()) {
            throw new DomainValidationException("iam.error.email.required");
        }
        if (codeHash == null || codeHash.isBlank()) {
            throw new DomainValidationException("iam.error.codeHash.required");
        }
        this.id = UUID.randomUUID();
        this.email = email.trim().toLowerCase();
        this.codeHash = codeHash;
        this.attempts = 0;
        this.status = EmailVerificationStatus.PENDING;
        this.verificationToken = null;
        this.createdAt = Instant.now();
        this.expiresAt = this.createdAt.plus(OTP_TTL);
        this.verifiedAt = null;
    }

    /**
     * Reconstitution constructor for persistence adapters.
     */
    public EmailVerificationSession(UUID id, String email, String codeHash, int attempts,
                                   EmailVerificationStatus status, String verificationToken,
                                   Instant createdAt, Instant expiresAt, Instant verifiedAt) {
        this.id = id;
        this.email = email != null ? email.trim().toLowerCase() : null;
        this.codeHash = codeHash;
        this.attempts = attempts;
        this.status = status;
        this.verificationToken = verificationToken;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.verifiedAt = verifiedAt;
    }

    /**
     * Validates input code against stored hash in constant time and transitions to VERIFIED if matching.
     */
    public void verify(boolean hashMatches) {
        if (this.status == EmailVerificationStatus.VERIFIED) {
            throw new DomainValidationException("iam.error.emailVerification.alreadyVerified");
        }
        if (this.status == EmailVerificationStatus.BLOCKED) {
            throw new DomainValidationException("iam.error.emailVerification.sessionBlocked");
        }
        if (this.status == EmailVerificationStatus.EXPIRED || isExpired()) {
            this.status = EmailVerificationStatus.EXPIRED;
            throw new DomainValidationException("iam.error.emailVerification.sessionExpired");
        }

        this.attempts++;

        if (!hashMatches) {
            if (this.attempts >= MAX_ATTEMPTS) {
                this.status = EmailVerificationStatus.BLOCKED;
                throw new DomainValidationException("iam.error.emailVerification.maxAttemptsExceeded");
            }
            throw new DomainValidationException("iam.error.emailVerification.invalidCode");
        }

        this.status = EmailVerificationStatus.VERIFIED;
        this.verifiedAt = Instant.now();
        this.verificationToken = UUID.randomUUID().toString();
    }

    public void expire() {
        this.status = EmailVerificationStatus.EXPIRED;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(this.expiresAt);
    }
}
