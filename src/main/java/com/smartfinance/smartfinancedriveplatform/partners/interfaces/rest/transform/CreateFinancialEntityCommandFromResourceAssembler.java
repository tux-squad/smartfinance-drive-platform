package com.smartfinance.smartfinancedriveplatform.partners.interfaces.rest.transform;

import com.smartfinance.smartfinancedriveplatform.partners.domain.model.commands.CreateFinancialEntityCommand;
import com.smartfinance.smartfinancedriveplatform.partners.interfaces.rest.resources.CreateFinancialEntityResource;

/**
 * Assembler to convert CreateFinancialEntityResource DTO into CreateFinancialEntityCommand.
 */
public final class CreateFinancialEntityCommandFromResourceAssembler {

    private CreateFinancialEntityCommandFromResourceAssembler() {}

    /**
     * Converts a CreateFinancialEntityResource to a CreateFinancialEntityCommand.
     *
     * @param resource The resource DTO.
     * @return The command.
     */
    public static CreateFinancialEntityCommand toCommandFromResource(CreateFinancialEntityResource resource) {
        return new CreateFinancialEntityCommand(resource.name());
    }
}
