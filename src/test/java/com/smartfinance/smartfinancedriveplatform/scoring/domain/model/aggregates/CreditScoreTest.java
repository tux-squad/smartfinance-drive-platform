package com.smartfinance.smartfinancedriveplatform.scoring.domain.model.aggregates;

import com.smartfinance.smartfinancedriveplatform.scoring.domain.model.valueobjects.RiskTier;
import com.smartfinance.smartfinancedriveplatform.scoring.domain.model.valueobjects.ScoringStatus;
import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.valueobjects.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CreditScore Aggregate Root Tests")
class CreditScoreTest {

    @Test
    @DisplayName("Should create CreditScore aggregate and automatically evaluate risk tier and status")
    void shouldCreateCreditScoreAndEvaluateRisk() {
        CreditScore score = new CreditScore(
                "profile-123",
                "simulation-456",
                Money.of(6000.0, "PEN"),
                Money.of(1500.0, "PEN") // DTI = 25%
        );

        assertNotNull(score.getId());
        assertEquals("profile-123", score.getProfileId());
        assertEquals("simulation-456", score.getSimulationId());
        assertEquals(RiskTier.TIER_A, score.getRiskTier());
        assertEquals(ScoringStatus.APPROVED, score.getStatus());
        assertNotNull(score.getAssessmentNotes());
    }

    @Test
    @DisplayName("Should throw exception when profileId is null or blank")
    void shouldThrowExceptionWhenProfileIdIsInvalid() {
        assertThrows(DomainValidationException.class, () -> new CreditScore(
                "",
                "simulation-456",
                Money.of(6000.0, "PEN"),
                Money.of(1500.0, "PEN")
        ));
    }
}
