package com.smartfinance.smartfinancedriveplatform.iam.infrastructure.persistence.jpa.entities;

import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.PhoneVerificationStatus;
import com.smartfinance.smartfinancedriveplatform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * JPA entity representing the 'phone_verification_sessions' table.
 */
@Entity
@Table(name = "phone_verification_sessions", indexes = {
        @Index(name = "idx_phone_verif_number", columnList = "phone_number"),
        @Index(name = "idx_phone_verif_status", columnList = "status"),
        @Index(name = "idx_phone_verif_user_id", columnList = "user_id"),
        @Index(name = "idx_phone_verif_token", columnList = "verification_token")
})
@Getter
@Setter
@NoArgsConstructor
public class PhoneVerificationSessionPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Column(name = "user_id")
    private String userId;

    @Column(name = "verification_token")
    private String verificationToken;

    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    @Column(name = "code_hash", nullable = false)
    private String codeHash;

    @Column(name = "attempts", nullable = false)
    private int attempts = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private PhoneVerificationStatus status = PhoneVerificationStatus.PENDING;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "verified_at")
    private Instant verifiedAt;
}
