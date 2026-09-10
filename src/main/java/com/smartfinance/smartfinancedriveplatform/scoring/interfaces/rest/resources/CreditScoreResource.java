package com.smartfinance.smartfinancedriveplatform.scoring.interfaces.rest.resources;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Resource DTO representing the response payload for a Credit Score evaluation.
 */
public record CreditScoreResource(
        UUID id,
        String profileId,
        String simulationId,
        String currency,
        BigDecimal monthlyIncomeAmount,
        BigDecimal projectedMonthlyInstallmentAmount,
        BigDecimal dtiRatio,
        String riskTier,
        BigDecimal rateAdjustment,
        String status,
        String assessmentNotes
) {}
