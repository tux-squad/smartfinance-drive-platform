package com.smartfinance.smartfinancedriveplatform.scoring.domain.model.aggregates;

import com.smartfinance.smartfinancedriveplatform.scoring.domain.model.valueobjects.RiskTier;
import com.smartfinance.smartfinancedriveplatform.scoring.domain.model.valueobjects.ScoreId;
import com.smartfinance.smartfinancedriveplatform.scoring.domain.model.valueobjects.ScoringStatus;
import com.smartfinance.smartfinancedriveplatform.scoring.domain.services.CreditScoringEngine;
import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.valueobjects.Money;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.valueobjects.Percent;
import lombok.Getter;

import java.util.UUID;

/**
 * CreditScore aggregate root representing a credit risk assessment evaluation for a customer profile.
 */
@Getter
public class CreditScore extends AbstractDomainAggregateRoot<CreditScore> {

    private final ScoreId id;
    private String profileId;
    private String simulationId;
    private Money monthlyIncome;
    private Money projectedMonthlyInstallment;
    private Percent dtiRatio;
    private RiskTier riskTier;
    private Percent rateAdjustment;
    private ScoringStatus status;
    private String assessmentNotes;

    /**
     * Reconstitution constructor from persistence.
     */
    public CreditScore(ScoreId id, String profileId, String simulationId, Money monthlyIncome,
                       Money projectedMonthlyInstallment, Percent dtiRatio, RiskTier riskTier,
                       Percent rateAdjustment, ScoringStatus status, String assessmentNotes) {
        this.id = id;
        this.profileId = profileId;
        this.simulationId = simulationId;
        this.monthlyIncome = monthlyIncome;
        this.projectedMonthlyInstallment = projectedMonthlyInstallment;
        this.dtiRatio = dtiRatio;
        this.riskTier = riskTier;
        this.rateAdjustment = rateAdjustment;
        this.status = status;
        this.assessmentNotes = assessmentNotes;
    }

    /**
     * Domain Constructor for creating a new CreditScore evaluation.
     */
    public CreditScore(String profileId, String simulationId, Money monthlyIncome, Money projectedMonthlyInstallment) {
        this.id = new ScoreId(UUID.randomUUID());
        setProfileId(profileId);
        this.simulationId = simulationId;
        this.monthlyIncome = monthlyIncome;
        this.projectedMonthlyInstallment = projectedMonthlyInstallment;

        // Perform automated scoring evaluation using CreditScoringEngine
        evaluateScore();
    }

    public void setProfileId(String profileId) {
        if (profileId == null || profileId.isBlank()) {
            throw new DomainValidationException("scoring.error.profileId.required");
        }
        this.profileId = profileId.trim();
    }

    public void evaluateScore() {
        CreditScoringEngine engine = new CreditScoringEngine();
        CreditScoringEngine.EvaluationResult result = engine.evaluateRisk(this.monthlyIncome, this.projectedMonthlyInstallment);

        this.dtiRatio = result.dtiRatio();
        this.riskTier = result.riskTier();
        this.rateAdjustment = result.rateAdjustment();
        this.status = result.status();
        this.assessmentNotes = result.assessmentNotes();
    }
}
