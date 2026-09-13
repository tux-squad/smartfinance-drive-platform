package com.smartfinance.smartfinancedriveplatform.scoring.interfaces.rest.resources;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * Resource DTO representing the request payload for evaluating a credit score.
 */
public record EvaluateCreditScoreResource(
        @NotBlank(message = "Profile ID is required")
        String profileId,

        @NotBlank(message = "Simulation ID is required")
        String simulationId,

        @NotNull(message = "Monthly income amount is required")
        @DecimalMin(value = "0.01", message = "Monthly income must be greater than zero")
        BigDecimal monthlyIncomeAmount,

        @NotNull(message = "Projected monthly installment amount is required")
        @DecimalMin(value = "0.01", message = "Projected monthly installment must be greater than zero")
        BigDecimal projectedMonthlyInstallmentAmount,

        @NotBlank(message = "Currency is required")
        @Size(min = 3, max = 3, message = "Currency must be a 3-letter code")
        String currency
) {}
