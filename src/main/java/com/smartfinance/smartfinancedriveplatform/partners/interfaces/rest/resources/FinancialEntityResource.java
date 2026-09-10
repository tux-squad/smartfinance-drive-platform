package com.smartfinance.smartfinancedriveplatform.partners.interfaces.rest.resources;

import java.util.List;
import java.util.UUID;

/**
 * Resource DTO representing the response payload for a financial entity.
 */
public record FinancialEntityResource(
    UUID id,
    String name,
    List<RateBenchmarkResource> rateBenchmarks
) {}
