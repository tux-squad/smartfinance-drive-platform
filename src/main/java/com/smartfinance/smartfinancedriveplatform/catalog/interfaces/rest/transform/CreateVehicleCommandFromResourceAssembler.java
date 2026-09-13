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
        return toCommandFromResource(resource, null);
    }

    /**
     * Converts a CreateVehicleResource and authenticated userId to a CreateVehicleCommand.
     *
     * @param resource            The resource DTO.
     * @param authenticatedUserId The authenticated user ID.
     * @return The command.
     */
    public static CreateVehicleCommand toCommandFromResource(CreateVehicleResource resource, String authenticatedUserId) {
        String targetUserId = (authenticatedUserId != null && !authenticatedUserId.isBlank())
                ? authenticatedUserId
                : resource.userId();

        return new CreateVehicleCommand(
            new UserId(targetUserId),
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
