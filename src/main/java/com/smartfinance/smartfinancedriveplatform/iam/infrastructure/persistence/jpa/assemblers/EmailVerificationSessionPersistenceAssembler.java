package com.smartfinance.smartfinancedriveplatform.iam.infrastructure.persistence.jpa.assemblers;

import com.smartfinance.smartfinancedriveplatform.iam.domain.model.aggregates.EmailVerificationSession;
import com.smartfinance.smartfinancedriveplatform.iam.infrastructure.persistence.jpa.entities.EmailVerificationSessionPersistenceEntity;

/**
 * Assembler to map between EmailVerificationSession aggregate and EmailVerificationSessionPersistenceEntity.
 */
public class EmailVerificationSessionPersistenceAssembler {

    public static EmailVerificationSessionPersistenceEntity toEntity(EmailVerificationSession domain) {
        if (domain == null) return null;
        EmailVerificationSessionPersistenceEntity entity = new EmailVerificationSessionPersistenceEntity();
        entity.setId(domain.getId());
        entity.setEmail(domain.getEmail());
        entity.setCodeHash(domain.getCodeHash());
        entity.setAttempts(domain.getAttempts());
        entity.setStatus(domain.getStatus());
        entity.setVerificationToken(domain.getVerificationToken());
        entity.setExpiresAt(domain.getExpiresAt());
        entity.setVerifiedAt(domain.getVerifiedAt());
        return entity;
    }

    public static EmailVerificationSession toDomain(EmailVerificationSessionPersistenceEntity entity) {
        if (entity == null) return null;
        return new EmailVerificationSession(
                entity.getId(),
                entity.getEmail(),
                entity.getCodeHash(),
                entity.getAttempts(),
                entity.getStatus(),
                entity.getVerificationToken(),
                entity.getCreatedAt(),
                entity.getExpiresAt(),
                entity.getVerifiedAt()
        );
    }
}
