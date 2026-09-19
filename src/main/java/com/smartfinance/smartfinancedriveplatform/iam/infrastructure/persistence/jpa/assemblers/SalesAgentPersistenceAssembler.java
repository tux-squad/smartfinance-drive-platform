package com.smartfinance.smartfinancedriveplatform.iam.infrastructure.persistence.jpa.assemblers;

import com.smartfinance.smartfinancedriveplatform.iam.domain.model.aggregates.SalesAgent;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.SalesAgentId;
import com.smartfinance.smartfinancedriveplatform.iam.infrastructure.persistence.jpa.entities.SalesAgentPersistenceEntity;

public class SalesAgentPersistenceAssembler {

    public static SalesAgentPersistenceEntity toEntity(SalesAgent aggregate) {
        if (aggregate == null) return null;
        return new SalesAgentPersistenceEntity(
                aggregate.getId().value(),
                aggregate.getDealerUserId(),
                aggregate.getFullName(),
                aggregate.getEmail(),
                aggregate.getPhone(),
                aggregate.isActive()
        );
    }

    public static SalesAgent toDomain(SalesAgentPersistenceEntity entity) {
        if (entity == null) return null;
        return new SalesAgent(
                new SalesAgentId(entity.getId()),
                entity.getDealerUserId(),
                entity.getFullName(),
                entity.getEmail(),
                entity.getPhone(),
                entity.isActive()
        );
    }
}
