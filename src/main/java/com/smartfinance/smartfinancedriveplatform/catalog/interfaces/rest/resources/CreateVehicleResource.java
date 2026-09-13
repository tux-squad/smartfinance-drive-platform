package com.smartfinance.smartfinancedriveplatform.catalog.interfaces.rest.resources;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Resource DTO representing the request payload to register a new vehicle.
 */
public record CreateVehicleResource(
    @NotBlank(message = "User ID is required")
    String userId,

    @NotNull(message = "Financial entity ID is required")
    UUID financialEntityId,

    @NotBlank(message = "Brand is required")
    @Size(max = 50, message = "Brand cannot exceed 50 characters")
    String brand,

    @NotBlank(message = "Model is required")
    @Size(max = 50, message = "Model cannot exceed 50 characters")
    String model,

    @Min(value = 1900, message = "Manufacture year must be at least 1900")
    @Max(value = 2100, message = "Manufacture year is invalid")
    int manufactureYear,

    @NotBlank(message = "Condition is required")
    @Size(max = 30, message = "Condition cannot exceed 30 characters")
    String condition,

    @NotNull(message = "Price amount is required")
    @DecimalMin(value = "0.01", message = "Price amount must be greater than zero")
    BigDecimal priceAmount,

    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be a 3-letter code (e.g., USD, PEN)")
    String currency,

    String imagePath
) {}
