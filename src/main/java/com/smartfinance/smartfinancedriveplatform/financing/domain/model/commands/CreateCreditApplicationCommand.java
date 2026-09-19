package com.smartfinance.smartfinancedriveplatform.financing.domain.model.commands;

import com.smartfinance.smartfinancedriveplatform.shared.domain.model.valueobjects.Money;

import java.util.UUID;

/**
 * Command to request submitting a formal credit application.
 */
public record CreateCreditApplicationCommand(
    String applicantUserId,
    UUID vehicleId,
    UUID financialEntityId,
    UUID simulationId,
    Money requestedAmount,
    Money downPayment,
    int termMonths,
    Money monthlyIncome,
    String employmentStatus
) {}
