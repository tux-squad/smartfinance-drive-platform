package com.smartfinance.smartfinancedriveplatform.profiles.interfaces.rest.transform;

import com.smartfinance.smartfinancedriveplatform.profiles.domain.model.commands.CreateProfileCommand;
import com.smartfinance.smartfinancedriveplatform.profiles.domain.model.valueobjects.UserId;
import com.smartfinance.smartfinancedriveplatform.profiles.interfaces.rest.resources.CreateProfileResource;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.valueobjects.Money;

/**
 * Assembler to convert CreateProfileResource DTO into CreateProfileCommand.
 */
public final class CreateProfileCommandFromResourceAssembler {

    private CreateProfileCommandFromResourceAssembler() {}

    /**
     * Converts a CreateProfileResource to a CreateProfileCommand.
     *
     * @param resource The resource DTO.
     * @return The command.
     */
    public static CreateProfileCommand toCommandFromResource(CreateProfileResource resource) {
        Money monthlyIncome = null;
        if (resource.monthlyIncomeAmount() != null && resource.monthlyIncomeCurrency() != null) {
            monthlyIncome = new Money(resource.monthlyIncomeAmount(), resource.monthlyIncomeCurrency());
        }

        return new CreateProfileCommand(
            new UserId(resource.userId()),
            resource.email(),
            resource.nationalId(),
            resource.fullLegalNames(),
            resource.dateOfBirth(),
            resource.phoneCountryCode(),
            resource.mobilePhone(),
            monthlyIncome,
            resource.employmentStatus()
        );
    }
}
