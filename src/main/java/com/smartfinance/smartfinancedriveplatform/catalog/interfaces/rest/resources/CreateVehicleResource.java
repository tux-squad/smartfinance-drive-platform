package com.smartfinance.smartfinancedriveplatform.catalog.interfaces.rest.resources;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Resource DTO representing the request payload to register a new vehicle.
 */
public record CreateVehicleResource(
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
