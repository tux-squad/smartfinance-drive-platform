package com.smartfinance.smartfinancedriveplatform.catalog.interfaces.rest.resources;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
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
    String imagePath,
    String status,
    Integer mileage,
    String transmission,
    String engine,
    String traction,
    List<String> images,
    Instant createdAt
) {
    public VehicleResource(UUID id, String userId, UUID financialEntityId, String brand, String model, int manufactureYear, String condition, BigDecimal priceAmount, String currency, String imagePath) {
        this(id, userId, financialEntityId, brand, model, manufactureYear, condition, priceAmount, currency, imagePath, "ACTIVE", 0, null, null, null, null, Instant.now());
    }

    public VehicleResource(UUID id, String userId, UUID financialEntityId, String brand, String model, int manufactureYear, String condition, BigDecimal priceAmount, String currency, String imagePath, String status, Integer mileage, String transmission, String engine, String traction, List<String> images) {
        this(id, userId, financialEntityId, brand, model, manufactureYear, condition, priceAmount, currency, imagePath, status, mileage, transmission, engine, traction, images, Instant.now());
    }
}



