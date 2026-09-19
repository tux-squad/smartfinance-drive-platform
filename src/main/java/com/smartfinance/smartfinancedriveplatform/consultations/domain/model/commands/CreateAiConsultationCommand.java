package com.smartfinance.smartfinancedriveplatform.consultations.domain.model.commands;

import com.smartfinance.smartfinancedriveplatform.shared.domain.model.valueobjects.Money;

public record CreateAiConsultationCommand(
    String userId,
    String prompt,
    Money monthlyIncome,
    Money maxBudget
) {}
