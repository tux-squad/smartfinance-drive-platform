package com.smartfinance.smartfinancedriveplatform.catalog.interfaces.rest.transform;

import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.aggregates.Vehicle;
import com.smartfinance.smartfinancedriveplatform.catalog.interfaces.rest.resources.VehicleResource;

/**
 * Assembler to convert a Vehicle domain aggregate root into a VehicleResource DTO.
 */
public final class VehicleResourceFromEntityAssembler {

    private VehicleResourceFromEntityAssembler() {}

    /**
     * Converts a Vehicle domain aggregate to a VehicleResource response DTO.
     *
     * @param vehicle The domain aggregate.
     * @return The resource DTO.
     */
    public static VehicleResource toResourceFromEntity(Vehicle vehicle) {
        return new VehicleResource(
            vehicle.getId().value(),
            vehicle.getUserId().value(),
            vehicle.getFinancialEntityId().value(),
            vehicle.getBrand(),
            vehicle.getModel(),
            vehicle.getManufactureYear(),
            vehicle.getCondition(),
            vehicle.getPrice().amount(),
            vehicle.getPrice().currency(),
            vehicle.getImagePath(),
            vehicle.getStatus(),
            vehicle.getMileage(),
            vehicle.getTransmission(),
            vehicle.getEngine(),
            vehicle.getTraction(),
            vehicle.getImages()
        );
    }
}

