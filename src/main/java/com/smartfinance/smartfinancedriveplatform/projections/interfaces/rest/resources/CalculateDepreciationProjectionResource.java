package com.smartfinance.smartfinancedriveplatform.projections.interfaces.rest.resources;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * Resource DTO representing the request payload for calculating a vehicle depreciation projection.
 */
public record CalculateDepreciationProjectionResource(
        @NotBlank(message = "Vehicle ID is required")
        String vehicleId,

        @NotBlank(message = "Simulation ID is required")
        String simulationId,

        @NotNull(message = "Initial vehicle price amount is required")
        @DecimalMin(value = "0.01", message = "Initial vehicle price must be greater than zero")
        BigDecimal initialVehiclePriceAmount,

        @NotBlank(message = "Currency is required")
        @Size(min = 3, max = 3, message = "Currency must be a 3-letter code")
        String currency,

        @Min(value = 1900, message = "Manufacture year must be at least 1900")
        @Max(value = 2100, message = "Manufacture year is invalid")
        int manufactureYear,

        @NotBlank(message = "Motorization type is required")
        String motorizationType,

        @NotNull(message = "Balloon payment amount is required")
        @DecimalMin(value = "0.00", message = "Balloon payment amount cannot be negative")
        BigDecimal balloonPaymentAmount
) {}
