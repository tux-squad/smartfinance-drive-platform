package com.smartfinance.smartfinancedriveplatform.financing.interfaces.rest.transform;

import com.smartfinance.smartfinancedriveplatform.financing.domain.model.aggregates.CreditApplication;
import com.smartfinance.smartfinancedriveplatform.financing.interfaces.rest.resources.CreditApplicationResource;

import java.math.BigDecimal;

/**
 * Assembler to convert CreditApplication aggregate into CreditApplicationResource DTO.
 */
public final class CreditApplicationResourceFromEntityAssembler {

    private CreditApplicationResourceFromEntityAssembler() {}

    public static CreditApplicationResource toResourceFromEntity(CreditApplication domain) {
        return new CreditApplicationResource(
            domain.getId().value(),
            domain.getApplicantUserId(),
            domain.getVehicleId(),
            domain.getFinancialEntityId(),
            domain.getSimulationId(),
            domain.getRequestedAmount().amount(),
            domain.getDownPayment() != null ? domain.getDownPayment().amount() : BigDecimal.ZERO,
            domain.getTermMonths(),
            domain.getMonthlyIncome().amount(),
            domain.getRequestedAmount().currency(),
            domain.getEmploymentStatus(),
            domain.getStatus(),
            domain.getNotes()
        );
    }
}
