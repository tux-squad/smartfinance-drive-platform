package com.smartfinance.smartfinancedriveplatform.scoring.infrastructure.persistence.jpa.assemblers;

import com.smartfinance.smartfinancedriveplatform.scoring.domain.model.aggregates.CreditScore;
import com.smartfinance.smartfinancedriveplatform.scoring.domain.model.valueobjects.RiskTier;
import com.smartfinance.smartfinancedriveplatform.scoring.domain.model.valueobjects.ScoreId;
import com.smartfinance.smartfinancedriveplatform.scoring.domain.model.valueobjects.ScoringStatus;
import com.smartfinance.smartfinancedriveplatform.scoring.infrastructure.persistence.jpa.entities.CreditScorePersistenceEntity;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.valueobjects.Money;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.valueobjects.Percent;

/**
 * Assembler class to convert between CreditScore domain aggregate and JPA persistence entities.
 */
public final class CreditScorePersistenceAssembler {

    private CreditScorePersistenceAssembler() {}

    public static CreditScorePersistenceEntity toEntity(CreditScore domain, CreditScorePersistenceEntity entity) {
        if (entity == null) {
            entity = new CreditScorePersistenceEntity();
        }
        entity.setId(domain.getId().value());
        entity.setProfileId(domain.getProfileId());
        entity.setSimulationId(domain.getSimulationId());
        entity.setCurrency(domain.getMonthlyIncome().currency());
        entity.setMonthlyIncomeAmount(domain.getMonthlyIncome().amount());
        entity.setProjectedMonthlyInstallmentAmount(domain.getProjectedMonthlyInstallment().amount());
        entity.setDtiRatio(domain.getDtiRatio().value());
        entity.setRiskTier(domain.getRiskTier().name());
        entity.setRateAdjustment(domain.getRateAdjustment().value());
        entity.setStatus(domain.getStatus().name());
        entity.setAssessmentNotes(domain.getAssessmentNotes());

        return entity;
    }

    public static CreditScore toDomain(CreditScorePersistenceEntity entity) {
        String currency = entity.getCurrency();

        return new CreditScore(
                new ScoreId(entity.getId()),
                entity.getProfileId(),
                entity.getSimulationId(),
                new Money(entity.getMonthlyIncomeAmount(), currency),
                new Money(entity.getProjectedMonthlyInstallmentAmount(), currency),
                new Percent(entity.getDtiRatio()),
                RiskTier.valueOf(entity.getRiskTier()),
                new Percent(entity.getRateAdjustment()),
                ScoringStatus.valueOf(entity.getStatus()),
                entity.getAssessmentNotes()
        );
    }
}
