package com.smartfinance.smartfinancedriveplatform.scoring.domain.model.commands;

import com.smartfinance.smartfinancedriveplatform.shared.domain.model.valueobjects.Money;

/**
 * Command to evaluate and create a new credit score for a customer profile.
 */
public record EvaluateCreditScoreCommand(
        String profileId,
        String simulationId,
        Money monthlyIncome,
        Money projectedMonthlyInstallment
) {}
