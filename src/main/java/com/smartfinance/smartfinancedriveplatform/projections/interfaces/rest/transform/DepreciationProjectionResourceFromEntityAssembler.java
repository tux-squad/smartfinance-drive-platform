package com.smartfinance.smartfinancedriveplatform.projections.interfaces.rest.transform;

import com.smartfinance.smartfinancedriveplatform.projections.domain.model.aggregates.DepreciationProjection;
import com.smartfinance.smartfinancedriveplatform.projections.interfaces.rest.resources.DepreciationProjectionResource;

/**
 * Assembler class to transform DepreciationProjection domain aggregate into DepreciationProjectionResource DTO.
 */
public final class DepreciationProjectionResourceFromEntityAssembler {

    private DepreciationProjectionResourceFromEntityAssembler() {}

    public static DepreciationProjectionResource toResourceFromEntity(DepreciationProjection entity) {
        return new DepreciationProjectionResource(
                entity.getId().value(),
                entity.getVehicleId(),
                entity.getSimulationId(),
                entity.getInitialVehiclePrice().currency(),
                entity.getInitialVehiclePrice().amount(),
                entity.getManufactureYear(),
                entity.getMotorizationType().name(),
                entity.getAnnualDepreciationRate().value(),
                entity.getProjectedValue2Years().amount(),
                entity.getProjectedValue3Years().amount(),
                entity.getProjectedValue5Years().amount(),
                entity.getBalloonPaymentAmount() != null ? entity.getBalloonPaymentAmount().amount() : null,
                entity.getRecommendedAction().name(),
                entity.getAdvisoryNotes()
        );
    }
}
