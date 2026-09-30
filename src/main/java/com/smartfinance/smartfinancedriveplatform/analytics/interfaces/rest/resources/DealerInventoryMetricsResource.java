package com.smartfinance.smartfinancedriveplatform.analytics.interfaces.rest.resources;

import java.math.BigDecimal;

public record DealerInventoryMetricsResource(
        int totalVehicles,
        int availableVehicles,
        int reservedVehicles,
        int soldVehicles,
        BigDecimal totalInventoryValuePen,
        BigDecimal totalInventoryValueUsd
) {}
