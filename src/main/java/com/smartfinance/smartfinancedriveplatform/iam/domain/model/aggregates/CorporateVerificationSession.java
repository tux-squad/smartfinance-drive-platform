package com.smartfinance.smartfinancedriveplatform.iam.domain.model.aggregates;

import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.CorporateVerificationSessionStatus;
import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

/**
 * Aggregate Root representing a B2B corporate verification session using email OTP.
 */
@Getter
public class CorporateVerificationSession extends AbstractDomainAggregateRoot<CorporateVerificationSession> {

    public static final int MAX_ATTEMPTS = 3;

    private final UUID id;
    private final String userId;
    private final String ruc;
    private final String corporateEmail;
    private final String codeHash;
    private final String entityType;
    private final String targetRole;
    private final String legalName;
    private final String fiscalAddress;
    private int attempts;
    private CorporateVerificationSessionStatus status;
    private final Instant expiresAt;

    /**
     * Constructor for reconstituting from persistence.
     */
    public CorporateVerificationSession(UUID id, String userId, String ruc, String corporateEmail,
                                       String codeHash, String entityType, String targetRole,
                                       String legalName, String fiscalAddress, int attempts,
                                       CorporateVerificationSessionStatus status, Instant expiresAt) {
        this.id = id;
        this.userId = userId;
        this.ruc = ruc;
        this.corporateEmail = corporateEmail;
        this.codeHash = codeHash;
        this.entityType = entityType;
        this.targetRole = targetRole;
        this.legalName = legalName;
        this.fiscalAddress = fiscalAddress;
        this.attempts = attempts;
        this.status = status;
        this.expiresAt = expiresAt;
    }

    /**
     * Factory constructor for creating a new verification session.
     */
    public CorporateVerificationSession(String userId, String ruc, String corporateEmail,
                                       String codeHash, String entityType, String targetRole,
                                       String legalName, String fiscalAddress, Instant expiresAt) {
        if (userId == null || userId.isBlank()) {
            throw new DomainValidationException("iam.error.userId.required");
        }
        if (ruc == null || !ruc.matches("^\\d{11}$")) {
            throw new DomainValidationException("iam.error.sunat.rucNotFound");
        }
        if (corporateEmail == null || !corporateEmail.contains("@")) {
            throw new DomainValidationException("iam.error.corporateEmail.invalid");
        }
        if (codeHash == null || codeHash.isBlank()) {
            throw new DomainValidationException("iam.error.otpCodeHash.required");
        }

        this.id = UUID.randomUUID();
        this.userId = userId.trim();
        this.ruc = ruc.trim();
        this.corporateEmail = corporateEmail.trim().toLowerCase();
        this.codeHash = codeHash.trim();
        this.entityType = entityType;
        this.targetRole = targetRole;
        this.legalName = legalName != null ? legalName.trim() : "";
        this.fiscalAddress = fiscalAddress != null ? fiscalAddress.trim() : "";
        this.attempts = 0;
        this.status = CorporateVerificationSessionStatus.PENDING;
        this.expiresAt = expiresAt;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public boolean isBlocked() {
        return attempts >= MAX_ATTEMPTS || status == CorporateVerificationSessionStatus.BLOCKED;
    }

    public boolean isPending() {
        if (status != CorporateVerificationSessionStatus.PENDING) {
            return false;
        }
        if (isExpired()) {
            this.status = CorporateVerificationSessionStatus.EXPIRED;
            return false;
        }
        return !isBlocked();
    }

    public void recordFailedAttempt() {
        this.attempts++;
        if (this.attempts >= MAX_ATTEMPTS) {
            this.status = CorporateVerificationSessionStatus.BLOCKED;
        }
    }

    public void markVerified() {
        if (!isPending()) {
            throw new DomainValidationException("iam.error.corporateVerification.sessionNotActive");
        }
        this.status = CorporateVerificationSessionStatus.VERIFIED;
    }

    public void markExpired() {
        this.status = CorporateVerificationSessionStatus.EXPIRED;
    }
}
