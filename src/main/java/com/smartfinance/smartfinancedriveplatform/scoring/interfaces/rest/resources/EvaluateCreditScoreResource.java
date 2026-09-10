package com.smartfinance.smartfinancedriveplatform.scoring.interfaces.rest.resources;

import java.math.BigDecimal;

/**
 * Resource DTO representing the request payload for evaluating a credit score.
 */
public record EvaluateCreditScoreResource(
        String profileId,
        String simulationId,
        BigDecimal monthlyIncomeAmount,
        BigDecimal projectedMonthlyInstallmentAmount,
        String currency
) {}
