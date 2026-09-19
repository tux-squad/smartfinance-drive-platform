package com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.transform;

import com.smartfinance.smartfinancedriveplatform.iam.domain.model.aggregates.SalesAgent;
import com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.resources.SalesAgentResource;

public class SalesAgentResourceFromEntityAssembler {

    public static SalesAgentResource toResourceFromEntity(SalesAgent entity) {
        if (entity == null) return null;
        return new SalesAgentResource(
                entity.getId().value(),
                entity.getDealerUserId(),
                entity.getFullName(),
                entity.getEmail(),
                entity.getPhone(),
                entity.isActive()
        );
    }
}
