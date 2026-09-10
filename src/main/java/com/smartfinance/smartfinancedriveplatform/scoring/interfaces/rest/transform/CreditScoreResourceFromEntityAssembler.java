package com.smartfinance.smartfinancedriveplatform.scoring.interfaces.rest.transform;

import com.smartfinance.smartfinancedriveplatform.scoring.domain.model.aggregates.CreditScore;
import com.smartfinance.smartfinancedriveplatform.scoring.interfaces.rest.resources.CreditScoreResource;

/**
 * Assembler class to transform CreditScore domain aggregate into CreditScoreResource DTO.
 */
public final class CreditScoreResourceFromEntityAssembler {

    private CreditScoreResourceFromEntityAssembler() {}

    public static CreditScoreResource toResourceFromEntity(CreditScore entity) {
        return new CreditScoreResource(
                entity.getId().value(),
                entity.getProfileId(),
                entity.getSimulationId(),
                entity.getMonthlyIncome().currency(),
                entity.getMonthlyIncome().amount(),
                entity.getProjectedMonthlyInstallment().amount(),
                entity.getDtiRatio().value(),
                entity.getRiskTier().name(),
                entity.getRateAdjustment().value(),
                entity.getStatus().name(),
                entity.getAssessmentNotes()
        );
    }
}
