package com.smartfinance.smartfinancedriveplatform.profiles.interfaces.rest.transform;

import com.smartfinance.smartfinancedriveplatform.profiles.domain.model.commands.UpdateProfileCommand;
import com.smartfinance.smartfinancedriveplatform.profiles.domain.model.valueobjects.ProfileId;
import com.smartfinance.smartfinancedriveplatform.profiles.interfaces.rest.resources.UpdateProfileResource;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.valueobjects.Money;

import java.util.UUID;

/**
 * Assembler to convert UpdateProfileResource DTO and profileId UUID into UpdateProfileCommand.
 */
public final class UpdateProfileCommandFromResourceAssembler {

    private UpdateProfileCommandFromResourceAssembler() {}

    /**
     * Converts a ProfileId UUID and UpdateProfileResource to an UpdateProfileCommand.
     *
     * @param profileId The profile UUID.
     * @param resource  The resource DTO.
     * @return The command.
     */
    public static UpdateProfileCommand toCommandFromResource(UUID profileId, UpdateProfileResource resource) {
        Money monthlyIncome = null;
        if (resource.monthlyIncomeAmount() != null && resource.monthlyIncomeCurrency() != null) {
            monthlyIncome = new Money(resource.monthlyIncomeAmount(), resource.monthlyIncomeCurrency());
        }

        return new UpdateProfileCommand(
            new ProfileId(profileId),
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
