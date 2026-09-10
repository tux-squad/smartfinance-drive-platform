package com.smartfinance.smartfinancedriveplatform.partners.interfaces.rest.transform;

import com.smartfinance.smartfinancedriveplatform.partners.domain.model.commands.UpdateFinancialEntityCommand;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.valueobjects.FinancialEntityId;
import com.smartfinance.smartfinancedriveplatform.partners.interfaces.rest.resources.UpdateFinancialEntityResource;

import java.util.UUID;

/**
 * Assembler to convert UpdateFinancialEntityResource DTO into UpdateFinancialEntityCommand.
 */
public final class UpdateFinancialEntityCommandFromResourceAssembler {

    private UpdateFinancialEntityCommandFromResourceAssembler() {}

    /**
     * Converts a financialEntityId UUID and UpdateFinancialEntityResource to an UpdateFinancialEntityCommand.
     *
     * @param id       The financial entity UUID.
     * @param resource The resource DTO.
     * @return The command.
     */
    public static UpdateFinancialEntityCommand toCommandFromResource(UUID id, UpdateFinancialEntityResource resource) {
        return new UpdateFinancialEntityCommand(
            new FinancialEntityId(id),
            resource.name()
        );
    }
}
