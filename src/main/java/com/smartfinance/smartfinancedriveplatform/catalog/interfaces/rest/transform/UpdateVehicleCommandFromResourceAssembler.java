package com.smartfinance.smartfinancedriveplatform.catalog.interfaces.rest.transform;

import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.commands.UpdateVehicleCommand;
import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.valueobjects.FinancialEntityId;
import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.valueobjects.VehicleId;
import com.smartfinance.smartfinancedriveplatform.catalog.interfaces.rest.resources.UpdateVehicleResource;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.valueobjects.Money;

import java.util.UUID;

/**
 * Assembler to convert an UpdateVehicleResource DTO and vehicleId path variable into an UpdateVehicleCommand.
 */
public final class UpdateVehicleCommandFromResourceAssembler {

    private UpdateVehicleCommandFromResourceAssembler() {}

    /**
     * Converts an UpdateVehicleResource and a UUID vehicleId to an UpdateVehicleCommand.
     *
     * @param vehicleId The vehicle identifier UUID.
     * @param resource  The resource DTO.
     * @return The command.
     */
    public static UpdateVehicleCommand toCommandFromResource(UUID vehicleId, UpdateVehicleResource resource) {
        return new UpdateVehicleCommand(
            new VehicleId(vehicleId),
            new FinancialEntityId(resource.financialEntityId()),
            resource.brand(),
            resource.model(),
            resource.manufactureYear(),
            resource.condition(),
            new Money(resource.priceAmount(), resource.currency()),
            resource.imagePath(),
            resource.status(),
            resource.mileage(),
            resource.transmission(),
            resource.engine(),
            resource.traction(),
            resource.images()
        );
    }
}

