package com.smartfinance.smartfinancedriveplatform.iam.infrastructure.persistence.jpa.assemblers;

import com.smartfinance.smartfinancedriveplatform.iam.domain.model.aggregates.CorporateVerificationSession;
import com.smartfinance.smartfinancedriveplatform.iam.infrastructure.persistence.jpa.entities.CorporateVerificationSessionPersistenceEntity;

/**
 * Assembler to convert between CorporateVerificationSession domain aggregate and JPA persistence entity.
 */
public final class CorporateVerificationSessionPersistenceAssembler {

    private CorporateVerificationSessionPersistenceAssembler() {}

    public static CorporateVerificationSessionPersistenceEntity toEntity(CorporateVerificationSession domain, CorporateVerificationSessionPersistenceEntity entity) {
        if (entity == null) {
            entity = new CorporateVerificationSessionPersistenceEntity();
        }
        entity.setId(domain.getId());
        entity.setUserId(domain.getUserId());
        entity.setRuc(domain.getRuc());
        entity.setCorporateEmail(domain.getCorporateEmail());
        entity.setCodeHash(domain.getCodeHash());
        entity.setEntityType(domain.getEntityType());
        entity.setTargetRole(domain.getTargetRole());
        entity.setLegalName(domain.getLegalName());
        entity.setFiscalAddress(domain.getFiscalAddress());
        entity.setAttempts(domain.getAttempts());
        entity.setStatus(domain.getStatus());
        entity.setExpiresAt(domain.getExpiresAt());
        return entity;
    }

    public static CorporateVerificationSession toDomain(CorporateVerificationSessionPersistenceEntity entity) {
        return new CorporateVerificationSession(
                entity.getId(),
                entity.getUserId(),
                entity.getRuc(),
                entity.getCorporateEmail(),
                entity.getCodeHash(),
                entity.getEntityType(),
                entity.getTargetRole(),
                entity.getLegalName(),
                entity.getFiscalAddress(),
                entity.getAttempts(),
                entity.getStatus(),
                entity.getExpiresAt()
        );
    }
}
