package com.smartfinance.smartfinancedriveplatform.iam.domain.model.aggregates;

import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.PhoneNumber;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.PhoneVerificationStatus;
import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * Aggregate Root representing a mobile phone verification session using OTP over WhatsApp.
 */
@Getter
public class PhoneVerificationSession extends AbstractDomainAggregateRoot<PhoneVerificationSession> {

    public static final int MAX_ATTEMPTS = 3;
    public static final Duration OTP_TTL = Duration.ofMinutes(5);
    public static final Duration RESEND_COOLDOWN = Duration.ofSeconds(60);

    private final UUID id;
    private final String userId;
    private final PhoneNumber phoneNumber;
    private final String codeHash;
    private int attempts;
    private PhoneVerificationStatus status;
    private final Instant createdAt;
    private final Instant expiresAt;
    private Instant verifiedAt;

    /**
     * Constructor for reconstituting from persistence with userId.
     */
    public PhoneVerificationSession(UUID id, String userId, PhoneNumber phoneNumber, String codeHash,
                                   int attempts, PhoneVerificationStatus status,
                                   Instant createdAt, Instant expiresAt, Instant verifiedAt) {
        this.id = id;
        this.userId = userId != null && !userId.isBlank() ? userId.trim() : null;
        this.phoneNumber = phoneNumber;
        this.codeHash = codeHash;
        this.attempts = attempts;
        this.status = status;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.verifiedAt = verifiedAt;
    }

    /**
     * Overload for reconstituting without explicit userId.
     */
    public PhoneVerificationSession(UUID id, PhoneNumber phoneNumber, String codeHash,
                                   int attempts, PhoneVerificationStatus status,
                                   Instant createdAt, Instant expiresAt, Instant verifiedAt) {
        this(id, null, phoneNumber, codeHash, attempts, status, createdAt, expiresAt, verifiedAt);
    }

    /**
     * Factory constructor for creating a new phone verification session with optional userId.
     */
    public PhoneVerificationSession(String userId, PhoneNumber phoneNumber, String codeHash, Instant createdAt, Instant expiresAt) {
        if (phoneNumber == null) {
            throw new DomainValidationException("iam.error.phoneNumber.required");
        }
        if (codeHash == null || codeHash.isBlank()) {
            throw new DomainValidationException("iam.error.otpCodeHash.required");
        }

        this.id = UUID.randomUUID();
        this.userId = userId != null && !userId.isBlank() ? userId.trim() : null;
        this.phoneNumber = phoneNumber;
        this.codeHash = codeHash.trim();
        this.attempts = 0;
        this.status = PhoneVerificationStatus.PENDING;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.expiresAt = expiresAt != null ? expiresAt : this.createdAt.plus(OTP_TTL);
        this.verifiedAt = null;
    }

    /**
     * Overload factory constructor for anonymous / pre-registration verification.
     */
    public PhoneVerificationSession(PhoneNumber phoneNumber, String codeHash, Instant createdAt, Instant expiresAt) {
        this(null, phoneNumber, codeHash, createdAt, expiresAt);
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public boolean isBlocked() {
        return attempts >= MAX_ATTEMPTS || status == PhoneVerificationStatus.BLOCKED;
    }

    public boolean isPending() {
        if (status != PhoneVerificationStatus.PENDING) {
            return false;
        }
        if (isExpired()) {
            this.status = PhoneVerificationStatus.EXPIRED;
            return false;
        }
        return !isBlocked();
    }

    public void recordFailedAttempt() {
        this.attempts++;
        if (this.attempts >= MAX_ATTEMPTS) {
            this.status = PhoneVerificationStatus.BLOCKED;
        }
    }

    public void markVerified() {
        if (!isPending()) {
            throw new DomainValidationException("iam.error.phoneVerification.sessionNotActive");
        }
        this.status = PhoneVerificationStatus.VERIFIED;
        this.verifiedAt = Instant.now();
    }

    public void markExpired() {
        this.status = PhoneVerificationStatus.EXPIRED;
    }

    public boolean canResend(Instant now) {
        Instant checkTime = now != null ? now : Instant.now();
        return checkTime.isAfter(createdAt.plus(RESEND_COOLDOWN));
    }
}
