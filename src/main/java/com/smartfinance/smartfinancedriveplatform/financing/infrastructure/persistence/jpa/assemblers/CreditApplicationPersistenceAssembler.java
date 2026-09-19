package com.smartfinance.smartfinancedriveplatform.financing.infrastructure.persistence.jpa.assemblers;

import com.smartfinance.smartfinancedriveplatform.financing.domain.model.aggregates.CreditApplication;
import com.smartfinance.smartfinancedriveplatform.financing.domain.model.valueobjects.CreditApplicationId;
import com.smartfinance.smartfinancedriveplatform.financing.infrastructure.persistence.jpa.entities.CreditApplicationPersistenceEntity;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.valueobjects.Money;

import java.math.BigDecimal;

/**
 * Assembler to convert between CreditApplication domain aggregate and JPA entity.
 */
public final class CreditApplicationPersistenceAssembler {

    private CreditApplicationPersistenceAssembler() {}

    public static CreditApplicationPersistenceEntity toEntity(CreditApplication domain, CreditApplicationPersistenceEntity entity) {
        if (entity == null) {
            entity = new CreditApplicationPersistenceEntity();
        }
        entity.setId(domain.getId().value());
        entity.setApplicantUserId(domain.getApplicantUserId());
        entity.setVehicleId(domain.getVehicleId());
        entity.setFinancialEntityId(domain.getFinancialEntityId());
        entity.setSimulationId(domain.getSimulationId());
        entity.setRequestedAmount(domain.getRequestedAmount().amount());
        entity.setCurrency(domain.getRequestedAmount().currency());
        entity.setDownPayment(domain.getDownPayment() != null ? domain.getDownPayment().amount() : BigDecimal.ZERO);
        entity.setTermMonths(domain.getTermMonths());
        entity.setMonthlyIncome(domain.getMonthlyIncome().amount());
        entity.setEmploymentStatus(domain.getEmploymentStatus());
        entity.setStatus(domain.getStatus());
        entity.setNotes(domain.getNotes());
        return entity;
    }

    public static CreditApplication toDomain(CreditApplicationPersistenceEntity entity) {
        return new CreditApplication(
            new CreditApplicationId(entity.getId()),
            entity.getApplicantUserId(),
            entity.getVehicleId(),
            entity.getFinancialEntityId(),
            entity.getSimulationId(),
            new Money(entity.getRequestedAmount(), entity.getCurrency()),
            entity.getDownPayment() != null ? new Money(entity.getDownPayment(), entity.getCurrency()) : new Money(BigDecimal.ZERO, entity.getCurrency()),
            entity.getTermMonths(),
            new Money(entity.getMonthlyIncome(), entity.getCurrency()),
            entity.getEmploymentStatus(),
            entity.getStatus(),
            entity.getNotes()
        );
    }
}
