package com.smartfinance.smartfinancedriveplatform.analytics.domain.model.valueobjects;

import java.math.BigDecimal;

/**
 * Value Object representing aggregated vehicle inventory metrics for a dealership.
 */
public record DealerInventoryMetrics(
    int totalVehicles,
    int availableVehicles,
    int reservedVehicles,
    int soldVehicles,
    BigDecimal totalInventoryValuePen,
    BigDecimal totalInventoryValueUsd
) {}
