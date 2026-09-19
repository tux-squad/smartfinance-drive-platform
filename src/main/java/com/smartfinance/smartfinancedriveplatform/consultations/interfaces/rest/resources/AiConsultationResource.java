package com.smartfinance.smartfinancedriveplatform.consultations.interfaces.rest.resources;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AiConsultationResource(
    UUID id,
    String userId,
    String prompt,
    BigDecimal monthlyIncome,
    BigDecimal maxBudget,
    String currency,
    String recommendationText,
    String recommendedVehicleCategory,
    Double estimatedMaxMonthlyFee,
    Instant createdAt
) {}
