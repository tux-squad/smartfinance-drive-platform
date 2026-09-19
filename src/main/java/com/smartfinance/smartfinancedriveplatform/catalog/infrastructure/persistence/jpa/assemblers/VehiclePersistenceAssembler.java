package com.smartfinance.smartfinancedriveplatform.catalog.infrastructure.persistence.jpa.assemblers;

import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.aggregates.Vehicle;
import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.valueobjects.FinancialEntityId;
import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.valueobjects.UserId;
import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.valueobjects.VehicleId;
import com.smartfinance.smartfinancedriveplatform.catalog.infrastructure.persistence.jpa.entities.VehiclePersistenceEntity;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.valueobjects.Money;

import java.util.ArrayList;

/**
 * Assembler class to convert between the Vehicle domain model and the JPA persistence entity.
 */
public final class VehiclePersistenceAssembler {

    private VehiclePersistenceAssembler() {}

    /**
     * Converts a Vehicle domain aggregate to a JPA persistence entity.
     *
     * @param domain The domain aggregate.
     * @param entity The target persistence entity (will create new if null).
     * @return The updated persistence entity.
     */
    public static VehiclePersistenceEntity toEntity(Vehicle domain, VehiclePersistenceEntity entity) {
        if (entity == null) {
            entity = new VehiclePersistenceEntity();
        }
        entity.setId(domain.getId().value());
        entity.setUserId(domain.getUserId().value());
        entity.setFinancialEntityId(domain.getFinancialEntityId().value());
        entity.setBrand(domain.getBrand());
        entity.setModel(domain.getModel());
        entity.setManufactureYear(domain.getManufactureYear());
        entity.setCondition(domain.getCondition());
        entity.setCurrency(domain.getPrice().currency());
        entity.setPrice(domain.getPrice().amount());
        entity.setImagePath(domain.getImagePath());
        entity.setStatus(domain.getStatus());
        entity.setMileage(domain.getMileage());
        entity.setTransmission(domain.getTransmission());
        entity.setEngine(domain.getEngine());
        entity.setTraction(domain.getTraction());
        entity.setImages(domain.getImages() != null ? new ArrayList<>(domain.getImages()) : new ArrayList<>());
        return entity;
    }

    /**
     * Converts a JPA persistence entity to a Vehicle domain aggregate.
     *
     * @param entity The persistence entity.
     * @return The domain aggregate.
     */
    public static Vehicle toDomain(VehiclePersistenceEntity entity) {
        return new Vehicle(
            new VehicleId(entity.getId()),
            new UserId(entity.getUserId()),
            new FinancialEntityId(entity.getFinancialEntityId()),
            entity.getBrand(),
            entity.getModel(),
            entity.getManufactureYear(),
            entity.getCondition(),
            new Money(entity.getPrice(), entity.getCurrency()),
            entity.getImagePath(),
            entity.getStatus(),
            entity.getMileage(),
            entity.getTransmission(),
            entity.getEngine(),
            entity.getTraction(),
            entity.getImages()
        );
    }
}

