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
        return toCommandFromResource(resource, null);
    }

    /**
     * Converts a CreateProfileResource and authenticated userId to a CreateProfileCommand.
     *
     * @param resource            The resource DTO.
     * @param authenticatedUserId The authenticated user ID.
     * @return The command.
     */
    public static CreateProfileCommand toCommandFromResource(CreateProfileResource resource, String authenticatedUserId) {
        Money monthlyIncome = null;
        if (resource.monthlyIncomeAmount() != null && resource.monthlyIncomeCurrency() != null) {
            monthlyIncome = new Money(resource.monthlyIncomeAmount(), resource.monthlyIncomeCurrency());
        }

        String targetUserId = (authenticatedUserId != null && !authenticatedUserId.isBlank())
                ? authenticatedUserId
                : resource.userId();

        return new CreateProfileCommand(
            new UserId(targetUserId),
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
