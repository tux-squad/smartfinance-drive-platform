package com.smartfinance.smartfinancedriveplatform.partners.interfaces.rest.transform;

import com.smartfinance.smartfinancedriveplatform.partners.domain.model.commands.AddRateBenchmarkCommand;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.valueobjects.FinancialEntityId;
import com.smartfinance.smartfinancedriveplatform.partners.interfaces.rest.resources.AddRateBenchmarkResource;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.valueobjects.Percent;

import java.util.UUID;

/**
 * Assembler to convert AddRateBenchmarkResource DTO into AddRateBenchmarkCommand.
 */
public final class AddRateBenchmarkCommandFromResourceAssembler {

    private AddRateBenchmarkCommandFromResourceAssembler() {}

    /**
     * Converts financialEntityId UUID and AddRateBenchmarkResource to an AddRateBenchmarkCommand.
     *
     * @param id       The financial entity UUID.
     * @param resource The resource DTO.
     * @return The command.
     */
    public static AddRateBenchmarkCommand toCommandFromResource(UUID id, AddRateBenchmarkResource resource) {
        return new AddRateBenchmarkCommand(
            new FinancialEntityId(id),
            resource.rateType(),
            new Percent(resource.annualRate()),
            resource.currency(),
            resource.sourceLabel(),
            resource.sourceUrl(),
            resource.effectiveFrom()
        );
    }
}
