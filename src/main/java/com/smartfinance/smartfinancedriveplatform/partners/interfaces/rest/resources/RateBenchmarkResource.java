package com.smartfinance.smartfinancedriveplatform.partners.interfaces.rest.resources;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Resource DTO representing the response payload for a rate benchmark.
 */
public record RateBenchmarkResource(
    UUID id,
    String rateType,
    BigDecimal annualRate,
    String currency,
    String sourceLabel,
    String sourceUrl,
    LocalDate effectiveFrom
) {}
