package com.smartfinance.smartfinancedriveplatform.projections.interfaces.rest.resources;

import java.math.BigDecimal;

/**
 * Resource DTO representing the request payload for calculating a vehicle depreciation projection.
 */
public record CalculateDepreciationProjectionResource(
        String vehicleId,
        String simulationId,
        BigDecimal initialVehiclePriceAmount,
        String currency,
        int manufactureYear,
        String motorizationType,
        BigDecimal balloonPaymentAmount
) {}
