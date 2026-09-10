package com.smartfinance.smartfinancedriveplatform.partners.interfaces.rest.resources;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Resource DTO representing the request payload to add a new rate benchmark to a financial entity.
 */
public record AddRateBenchmarkResource(
    String rateType,
    BigDecimal annualRate,
    String currency,
    String sourceLabel,
    String sourceUrl,
    LocalDate effectiveFrom
) {}
