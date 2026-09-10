package com.smartfinance.smartfinancedriveplatform.projections.interfaces.rest.resources;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Resource DTO representing the response payload for a Depreciation Projection.
 */
public record DepreciationProjectionResource(
        UUID id,
        String vehicleId,
        String simulationId,
        String currency,
        BigDecimal initialVehiclePriceAmount,
        int manufactureYear,
        String motorizationType,
        BigDecimal annualDepreciationRate,
        BigDecimal projectedValue2YearsAmount,
        BigDecimal projectedValue3YearsAmount,
        BigDecimal projectedValue5YearsAmount,
        BigDecimal balloonPaymentAmount,
        String recommendedAction,
        String advisoryNotes
) {}
