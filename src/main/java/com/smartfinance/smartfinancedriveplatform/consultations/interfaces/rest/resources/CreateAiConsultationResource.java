package com.smartfinance.smartfinancedriveplatform.consultations.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

public record CreateAiConsultationResource(
    @NotBlank(message = "Prompt query is required")
    String prompt,

    BigDecimal monthlyIncome,
    BigDecimal maxBudget,
    String currency
) {}
