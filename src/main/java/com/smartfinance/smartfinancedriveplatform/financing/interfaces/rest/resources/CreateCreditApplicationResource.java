package com.smartfinance.smartfinancedriveplatform.financing.interfaces.rest.resources;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Resource DTO for submitting a new formal credit application.
 */
public record CreateCreditApplicationResource(
    @NotNull(message = "Vehicle ID is required")
    UUID vehicleId,

    @NotNull(message = "Financial entity ID is required")
    UUID financialEntityId,

    UUID simulationId,

    @NotNull(message = "Requested amount is required")
    @DecimalMin(value = "0.01", message = "Requested amount must be greater than zero")
    BigDecimal requestedAmount,

    BigDecimal downPayment,

    @Min(value = 1, message = "Term months must be at least 1")
    @Max(value = 120, message = "Term months cannot exceed 120")
    int termMonths,

    @NotNull(message = "Monthly income is required")
    @DecimalMin(value = "0.01", message = "Monthly income must be greater than zero")
    BigDecimal monthlyIncome,

    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be a 3-letter code (e.g., USD, PEN)")
    String currency,

    @NotBlank(message = "Employment status is required")
    String employmentStatus
) {}
