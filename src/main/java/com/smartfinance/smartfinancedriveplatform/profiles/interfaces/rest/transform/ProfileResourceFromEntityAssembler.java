package com.smartfinance.smartfinancedriveplatform.profiles.interfaces.rest.transform;

import com.smartfinance.smartfinancedriveplatform.profiles.domain.model.aggregates.Profile;
import com.smartfinance.smartfinancedriveplatform.profiles.interfaces.rest.resources.ProfileResource;

import java.math.BigDecimal;

/**
 * Assembler to convert a Profile domain aggregate root into a ProfileResource response DTO.
 */
public final class ProfileResourceFromEntityAssembler {

    private ProfileResourceFromEntityAssembler() {}

    /**
     * Converts a Profile domain aggregate to a ProfileResource response DTO.
     *
     * @param profile The profile domain aggregate.
     * @return The resource DTO.
     */
    public static ProfileResource toResourceFromEntity(Profile profile) {
        BigDecimal incomeAmount = profile.getMonthlyIncome() != null ? profile.getMonthlyIncome().amount() : null;
        String incomeCurrency = profile.getMonthlyIncome() != null ? profile.getMonthlyIncome().currency() : null;

        return new ProfileResource(
            profile.getId().value(),
            profile.getUserId().value(),
            profile.getEmail(),
            profile.getNationalId(),
            profile.getFullLegalNames(),
            profile.getDateOfBirth(),
            profile.getPhoneCountryCode(),
            profile.getMobilePhone(),
            incomeAmount,
            incomeCurrency,
            profile.getEmploymentStatus()
        );
    }
}
