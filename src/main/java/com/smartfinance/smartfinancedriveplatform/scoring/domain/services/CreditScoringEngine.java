package com.smartfinance.smartfinancedriveplatform.scoring.domain.services;

import com.smartfinance.smartfinancedriveplatform.scoring.domain.model.valueobjects.RiskTier;
import com.smartfinance.smartfinancedriveplatform.scoring.domain.model.valueobjects.ScoringStatus;
import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.valueobjects.Money;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.valueobjects.Percent;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Domain Service responsible for calculating Debt-to-Income (DTI) ratio,
 * evaluating risk tiers (Tier A/B/C), and assigning rate adjustments.
 */
public class CreditScoringEngine {

    public record EvaluationResult(
            Percent dtiRatio,
            RiskTier riskTier,
            Percent rateAdjustment,
            ScoringStatus status,
            String assessmentNotes
    ) {}

    /**
     * Evaluates credit risk based on monthly net income and projected monthly installment.
     */
    public EvaluationResult evaluateRisk(Money monthlyIncome, Money projectedMonthlyInstallment) {
        if (monthlyIncome == null || monthlyIncome.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainValidationException("scoring.error.monthlyIncome.invalid");
        }
        if (projectedMonthlyInstallment == null || projectedMonthlyInstallment.amount().compareTo(BigDecimal.ZERO) < 0) {
            throw new DomainValidationException("scoring.error.monthlyInstallment.invalid");
        }

        BigDecimal income = monthlyIncome.amount();
        BigDecimal installment = projectedMonthlyInstallment.amount();

        BigDecimal dtiDecimal = installment.divide(income, 6, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
        Percent dtiRatio = Percent.of(dtiDecimal.doubleValue());

        double dtiValue = dtiDecimal.doubleValue();

        RiskTier tier;
        ScoringStatus status;
        Percent rateAdjustment;
        String notes;

        if (dtiValue <= 30.0) {
            tier = RiskTier.TIER_A;
            status = ScoringStatus.APPROVED;
            rateAdjustment = Percent.of(1.5);
            notes = String.format("Low risk profile (DTI %.2f%% <= 30%%). Qualifies for 1.5%% TEA discount.", dtiValue);
        } else if (dtiValue <= 45.0) {
            tier = RiskTier.TIER_B;
            status = ScoringStatus.CONDITIONALLY_APPROVED;
            rateAdjustment = Percent.of(0.0);
            notes = String.format("Medium risk profile (DTI %.2f%%). Standard TEA applies.", dtiValue);
        } else {
            tier = RiskTier.TIER_C;
            status = ScoringStatus.REJECTED;
            rateAdjustment = Percent.of(2.5);
            notes = String.format("High risk profile (DTI %.2f%% > 45%%). Exceeds recommended debt-to-income threshold.", dtiValue);
        }

        return new EvaluationResult(dtiRatio, tier, rateAdjustment, status, notes);
    }
}
