package com.smartfinance.smartfinancedriveplatform.profiles.infrastructure.persistence.jpa.assemblers;

import com.smartfinance.smartfinancedriveplatform.profiles.domain.model.aggregates.Profile;
import com.smartfinance.smartfinancedriveplatform.profiles.domain.model.valueobjects.ProfileId;
import com.smartfinance.smartfinancedriveplatform.profiles.domain.model.valueobjects.UserId;
import com.smartfinance.smartfinancedriveplatform.profiles.infrastructure.persistence.jpa.entities.ProfilePersistenceEntity;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.valueobjects.Money;

/**
 * Assembler class to convert between the Profile domain model and the JPA persistence entity.
 */
public final class ProfilePersistenceAssembler {

    private ProfilePersistenceAssembler() {}

    /**
     * Converts a Profile domain aggregate to a JPA persistence entity.
     *
     * @param domain The domain aggregate.
     * @param entity The target persistence entity (will create new if null).
     * @return The updated persistence entity.
     */
    public static ProfilePersistenceEntity toEntity(Profile domain, ProfilePersistenceEntity entity) {
        if (entity == null) {
            entity = new ProfilePersistenceEntity();
        }
        entity.setId(domain.getId().value());
        entity.setUserId(domain.getUserId().value());
        entity.setEmail(domain.getEmail());
        entity.setNationalId(domain.getNationalId());
        entity.setFullLegalNames(domain.getFullLegalNames());
        entity.setDateOfBirth(domain.getDateOfBirth());
        entity.setPhoneCountryCode(domain.getPhoneCountryCode());
        entity.setMobilePhone(domain.getMobilePhone());
        
        if (domain.getMonthlyIncome() != null) {
            entity.setMonthlyIncomeAmount(domain.getMonthlyIncome().amount());
            entity.setMonthlyIncomeCurrency(domain.getMonthlyIncome().currency());
        } else {
            entity.setMonthlyIncomeAmount(null);
            entity.setMonthlyIncomeCurrency(null);
        }
        
        entity.setEmploymentStatus(domain.getEmploymentStatus());
        return entity;
    }

    /**
     * Converts a JPA persistence entity to a Profile domain aggregate.
     *
     * @param entity The persistence entity.
     * @return The domain aggregate.
     */
    public static Profile toDomain(ProfilePersistenceEntity entity) {
        Money monthlyIncome = null;
        if (entity.getMonthlyIncomeAmount() != null && entity.getMonthlyIncomeCurrency() != null) {
            monthlyIncome = new Money(entity.getMonthlyIncomeAmount(), entity.getMonthlyIncomeCurrency());
        }

        return new Profile(
            new ProfileId(entity.getId()),
            new UserId(entity.getUserId()),
            entity.getEmail(),
            entity.getNationalId(),
            entity.getFullLegalNames(),
            entity.getDateOfBirth(),
            entity.getPhoneCountryCode(),
            entity.getMobilePhone(),
            monthlyIncome,
            entity.getEmploymentStatus()
        );
    }
}
