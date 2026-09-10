package com.smartfinance.smartfinancedriveplatform.scoring.domain.services;

import com.smartfinance.smartfinancedriveplatform.scoring.domain.model.valueobjects.RiskTier;
import com.smartfinance.smartfinancedriveplatform.scoring.domain.model.valueobjects.ScoringStatus;
import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.valueobjects.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CreditScoringEngine Domain Service Tests")
class CreditScoringEngineTest {

    private CreditScoringEngine engine;

    @BeforeEach
    void setUp() {
        engine = new CreditScoringEngine();
    }

    @Test
    @DisplayName("Should evaluate Tier A (Low Risk) when DTI <= 30%")
    void shouldEvaluateTierAWhenDtiIsLow() {
        Money monthlyIncome = Money.of(5000.0, "PEN");
        Money installment = Money.of(1200.0, "PEN"); // DTI = 24%

        CreditScoringEngine.EvaluationResult result = engine.evaluateRisk(monthlyIncome, installment);

        assertNotNull(result);
        assertEquals(RiskTier.TIER_A, result.riskTier());
        assertEquals(ScoringStatus.APPROVED, result.status());
        assertEquals(new BigDecimal("1.500000"), result.rateAdjustment().value());
        assertTrue(result.dtiRatio().value().compareTo(new BigDecimal("30.0")) <= 0);
    }

    @Test
    @DisplayName("Should evaluate Tier B (Medium Risk) when 30% < DTI <= 45%")
    void shouldEvaluateTierBWhenDtiIsMedium() {
        Money monthlyIncome = Money.of(4000.0, "PEN");
        Money installment = Money.of(1500.0, "PEN"); // DTI = 37.5%

        CreditScoringEngine.EvaluationResult result = engine.evaluateRisk(monthlyIncome, installment);

        assertNotNull(result);
        assertEquals(RiskTier.TIER_B, result.riskTier());
        assertEquals(ScoringStatus.CONDITIONALLY_APPROVED, result.status());
        assertEquals(new BigDecimal("0.000000"), result.rateAdjustment().value());
    }

    @Test
    @DisplayName("Should evaluate Tier C (High Risk) when DTI > 45%")
    void shouldEvaluateTierCWhenDtiIsHigh() {
        Money monthlyIncome = Money.of(3000.0, "PEN");
        Money installment = Money.of(1800.0, "PEN"); // DTI = 60%

        CreditScoringEngine.EvaluationResult result = engine.evaluateRisk(monthlyIncome, installment);

        assertNotNull(result);
        assertEquals(RiskTier.TIER_C, result.riskTier());
        assertEquals(ScoringStatus.REJECTED, result.status());
        assertEquals(new BigDecimal("2.500000"), result.rateAdjustment().value());
    }

    @Test
    @DisplayName("Should throw exception when monthly income is zero or negative")
    void shouldThrowExceptionWhenIncomeIsInvalid() {
        Money zeroIncome = Money.zero("PEN");
        Money installment = Money.of(500.0, "PEN");

        assertThrows(DomainValidationException.class, () -> engine.evaluateRisk(zeroIncome, installment));
    }
}
