package com.smartfinance.smartfinancedriveplatform.partners.interfaces.rest.resources;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Resource DTO representing the request payload to add a new rate benchmark to a financial entity.
 */
public record AddRateBenchmarkResource(
    @NotBlank(message = "Rate type is required")
    String rateType,

    @NotNull(message = "Annual rate is required")
    @DecimalMin(value = "0.00", message = "Annual rate cannot be negative")
    @DecimalMax(value = "100.00", message = "Annual rate cannot exceed 100%")
    BigDecimal annualRate,

    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be a 3-letter code")
    String currency,

    String sourceLabel,
    String sourceUrl,
    LocalDate effectiveFrom
) {}
