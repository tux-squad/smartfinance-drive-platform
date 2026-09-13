package com.smartfinance.smartfinancedriveplatform.financing.interfaces.rest.resources;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Resource DTO representing the request payload for creating a credit simulation.
 */
public record CreateSimulationResource(
        @NotBlank(message = "Title is required")
        @Size(max = 100, message = "Title cannot exceed 100 characters")
        String title,

        @NotBlank(message = "User ID is required")
        String userId,

        @NotBlank(message = "Vehicle ID is required")
        String vehicleId,

        @NotBlank(message = "Financial entity ID is required")
        String financialEntityId,

        @NotNull(message = "Vehicle price amount is required")
        @DecimalMin(value = "0.01", message = "Vehicle price must be greater than zero")
        BigDecimal vehiclePriceAmount,

        @NotBlank(message = "Currency is required")
        @Size(min = 3, max = 3, message = "Currency must be a 3-letter code")
        String currency,

        @NotNull(message = "Down payment percentage is required")
        @DecimalMin(value = "0.00", message = "Down payment percentage cannot be negative")
        @DecimalMax(value = "100.00", message = "Down payment percentage cannot exceed 100%")
        BigDecimal downPaymentPercentage,

        @NotNull(message = "Balloon payment percentage is required")
        @DecimalMin(value = "0.00", message = "Balloon payment percentage cannot be negative")
        @DecimalMax(value = "100.00", message = "Balloon payment percentage cannot exceed 100%")
        BigDecimal balloonPaymentPercentage,

        @NotNull(message = "Annual effective rate is required")
        @DecimalMin(value = "0.00", message = "Annual effective rate cannot be negative")
        @DecimalMax(value = "100.00", message = "Annual effective rate cannot exceed 100%")
        BigDecimal annualEffectiveRate,

        @DecimalMin(value = "0.00", message = "Monthly credit life insurance rate cannot be negative")
        @DecimalMax(value = "100.00", message = "Monthly credit life insurance rate cannot exceed 100%")
        BigDecimal monthlyCreditLifeInsuranceRate,

        @DecimalMin(value = "0.00", message = "Vehicle insurance fee cannot be negative")
        BigDecimal vehicleInsuranceFeeAmount,

        String vehicleInsuranceType,

        @Min(value = 1, message = "Loan term must be at least 1 month")
        @Max(value = 360, message = "Loan term cannot exceed 360 months")
        int loanTermMonths,

        @NotBlank(message = "Grace period type is required")
        String gracePeriodType,

        @Min(value = 0, message = "Grace period months cannot be negative")
        @Max(value = 60, message = "Grace period months cannot exceed 60")
        int gracePeriodMonths,

        @DecimalMin(value = "0.00", message = "Initial fees cannot be negative")
        BigDecimal initialFeesAmount,

        @DecimalMin(value = "0.00", message = "Discount rate cannot be negative")
        @DecimalMax(value = "100.00", message = "Discount rate cannot exceed 100%")
        BigDecimal discountRate,

        LocalDate startDate
) {}
