package com.smartfinance.smartfinancedriveplatform.iam.infrastructure.persistence.jpa.assemblers;

import com.smartfinance.smartfinancedriveplatform.iam.domain.model.aggregates.PhoneVerificationSession;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.PhoneNumber;
import com.smartfinance.smartfinancedriveplatform.iam.infrastructure.persistence.jpa.entities.PhoneVerificationSessionPersistenceEntity;

/**
 * Assembler to convert between PhoneVerificationSession domain aggregate and JPA persistence entity.
 */
public final class PhoneVerificationSessionPersistenceAssembler {

    private PhoneVerificationSessionPersistenceAssembler() {}

    public static PhoneVerificationSessionPersistenceEntity toEntity(PhoneVerificationSession domain, PhoneVerificationSessionPersistenceEntity entity) {
        if (entity == null) {
            entity = new PhoneVerificationSessionPersistenceEntity();
        }
        entity.setId(domain.getId());
        entity.setUserId(domain.getUserId());
        entity.setPhoneNumber(domain.getPhoneNumber().fullNumber());
        entity.setCodeHash(domain.getCodeHash());
        entity.setAttempts(domain.getAttempts());
        entity.setStatus(domain.getStatus());
        entity.setExpiresAt(domain.getExpiresAt());
        entity.setVerifiedAt(domain.getVerifiedAt());
        return entity;
    }

    public static PhoneVerificationSession toDomain(PhoneVerificationSessionPersistenceEntity entity) {
        return new PhoneVerificationSession(
                entity.getId(),
                entity.getUserId(),
                new PhoneNumber(entity.getPhoneNumber()),
                entity.getCodeHash(),
                entity.getAttempts(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getExpiresAt(),
                entity.getVerifiedAt()
        );
    }
}
