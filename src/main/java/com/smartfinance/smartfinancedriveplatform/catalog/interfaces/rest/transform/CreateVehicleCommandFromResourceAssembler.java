package com.smartfinance.smartfinancedriveplatform.catalog.interfaces.rest.transform;

import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.commands.CreateVehicleCommand;
import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.valueobjects.FinancialEntityId;
import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.valueobjects.UserId;
import com.smartfinance.smartfinancedriveplatform.catalog.interfaces.rest.resources.CreateVehicleResource;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.valueobjects.Money;

/**
 * Assembler to convert a CreateVehicleResource DTO into a CreateVehicleCommand.
 */
public final class CreateVehicleCommandFromResourceAssembler {

    private CreateVehicleCommandFromResourceAssembler() {}

    /**
     * Converts a CreateVehicleResource to a CreateVehicleCommand.
     *
     * @param resource The resource DTO.
     * @return The command.
     */
    public static CreateVehicleCommand toCommandFromResource(CreateVehicleResource resource) {
        return new CreateVehicleCommand(
            new UserId(resource.userId()),
            new FinancialEntityId(resource.financialEntityId()),
            resource.brand(),
            resource.model(),
            resource.manufactureYear(),
            resource.condition(),
            new Money(resource.priceAmount(), resource.currency()),
            resource.imagePath()
        );
    }
}
