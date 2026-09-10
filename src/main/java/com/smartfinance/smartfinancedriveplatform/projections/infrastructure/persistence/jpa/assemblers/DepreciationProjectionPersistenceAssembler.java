package com.smartfinance.smartfinancedriveplatform.projections.infrastructure.persistence.jpa.assemblers;

import com.smartfinance.smartfinancedriveplatform.projections.domain.model.aggregates.DepreciationProjection;
import com.smartfinance.smartfinancedriveplatform.projections.domain.model.valueobjects.MotorizationType;
import com.smartfinance.smartfinancedriveplatform.projections.domain.model.valueobjects.ProjectionId;
import com.smartfinance.smartfinancedriveplatform.projections.domain.model.valueobjects.RecommendedAction;
import com.smartfinance.smartfinancedriveplatform.projections.infrastructure.persistence.jpa.entities.DepreciationProjectionPersistenceEntity;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.valueobjects.Money;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.valueobjects.Percent;

/**
 * Assembler class to convert between DepreciationProjection domain aggregate and JPA persistence entities.
 */
public final class DepreciationProjectionPersistenceAssembler {

    private DepreciationProjectionPersistenceAssembler() {}

    public static DepreciationProjectionPersistenceEntity toEntity(DepreciationProjection domain, DepreciationProjectionPersistenceEntity entity) {
        if (entity == null) {
            entity = new DepreciationProjectionPersistenceEntity();
        }
        entity.setId(domain.getId().value());
        entity.setVehicleId(domain.getVehicleId());
        entity.setSimulationId(domain.getSimulationId());
        entity.setCurrency(domain.getInitialVehiclePrice().currency());
        entity.setInitialVehiclePriceAmount(domain.getInitialVehiclePrice().amount());
        entity.setManufactureYear(domain.getManufactureYear());
        entity.setMotorizationType(domain.getMotorizationType().name());
        entity.setAnnualDepreciationRate(domain.getAnnualDepreciationRate().value());
        entity.setProjectedValue2YearsAmount(domain.getProjectedValue2Years().amount());
        entity.setProjectedValue3YearsAmount(domain.getProjectedValue3Years().amount());
        entity.setProjectedValue5YearsAmount(domain.getProjectedValue5Years().amount());
        entity.setBalloonPaymentAmount(domain.getBalloonPaymentAmount() != null ? domain.getBalloonPaymentAmount().amount() : null);
        entity.setRecommendedAction(domain.getRecommendedAction().name());
        entity.setAdvisoryNotes(domain.getAdvisoryNotes());

        return entity;
    }

    public static DepreciationProjection toDomain(DepreciationProjectionPersistenceEntity entity) {
        String currency = entity.getCurrency();

        return new DepreciationProjection(
                new ProjectionId(entity.getId()),
                entity.getVehicleId(),
                entity.getSimulationId(),
                new Money(entity.getInitialVehiclePriceAmount(), currency),
                entity.getManufactureYear(),
                MotorizationType.valueOf(entity.getMotorizationType()),
                new Percent(entity.getAnnualDepreciationRate()),
                new Money(entity.getProjectedValue2YearsAmount(), currency),
                new Money(entity.getProjectedValue3YearsAmount(), currency),
                new Money(entity.getProjectedValue5YearsAmount(), currency),
                entity.getBalloonPaymentAmount() != null ? new Money(entity.getBalloonPaymentAmount(), currency) : null,
                RecommendedAction.valueOf(entity.getRecommendedAction()),
                entity.getAdvisoryNotes()
        );
    }
}
