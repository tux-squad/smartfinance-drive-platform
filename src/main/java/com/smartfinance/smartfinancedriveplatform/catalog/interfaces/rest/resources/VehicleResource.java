package com.smartfinance.smartfinancedriveplatform.catalog.interfaces.rest.resources;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Resource DTO representing the response payload for a vehicle.
 */
public record VehicleResource(
    UUID id,
    String userId,
    UUID financialEntityId,
    String brand,
    String model,
    int manufactureYear,
    String condition,
    BigDecimal priceAmount,
    String currency,
    String imagePath
) {}
