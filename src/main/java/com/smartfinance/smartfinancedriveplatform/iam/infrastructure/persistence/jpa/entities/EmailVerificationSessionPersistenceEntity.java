package com.smartfinance.smartfinancedriveplatform.iam.infrastructure.persistence.jpa.entities;

import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.EmailVerificationStatus;
import com.smartfinance.smartfinancedriveplatform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * JPA entity representing the 'email_verification_sessions' table.
 */
@Entity
@Table(name = "email_verification_sessions", indexes = {
        @Index(name = "idx_email_verif_email", columnList = "email"),
        @Index(name = "idx_email_verif_status", columnList = "status"),
        @Index(name = "idx_email_verif_token", columnList = "verification_token")
})
@Getter
@Setter
@NoArgsConstructor
public class EmailVerificationSessionPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Column(name = "code_hash", nullable = false)
    private String codeHash;

    @Column(name = "attempts", nullable = false)
    private int attempts = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private EmailVerificationStatus status = EmailVerificationStatus.PENDING;

    @Column(name = "verification_token")
    private String verificationToken;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "verified_at")
    private Instant verifiedAt;
}
