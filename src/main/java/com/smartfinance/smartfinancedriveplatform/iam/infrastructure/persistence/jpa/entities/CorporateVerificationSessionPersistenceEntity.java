package com.smartfinance.smartfinancedriveplatform.iam.infrastructure.persistence.jpa.entities;

import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.CorporateVerificationSessionStatus;
import com.smartfinance.smartfinancedriveplatform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * JPA entity representing the 'corporate_verification_sessions' table.
 */
@Entity
@Table(name = "corporate_verification_sessions", indexes = {
        @Index(name = "idx_corp_verif_user_ruc", columnList = "user_id, ruc"),
        @Index(name = "idx_corp_verif_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
public class CorporateVerificationSessionPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "ruc", nullable = false, length = 11)
    private String ruc;

    @Column(name = "corporate_email", nullable = false)
    private String corporateEmail;

    @Column(name = "code_hash", nullable = false)
    private String codeHash;

    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType;

    @Column(name = "target_role", nullable = false, length = 50)
    private String targetRole;

    @Column(name = "legal_name", nullable = false)
    private String legalName;

    @Column(name = "fiscal_address", length = 500)
    private String fiscalAddress;

    @Column(name = "attempts", nullable = false)
    private int attempts = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private CorporateVerificationSessionStatus status = CorporateVerificationSessionStatus.PENDING;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;
}
