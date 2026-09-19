package com.smartfinance.smartfinancedriveplatform.catalog.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Resource DTO representing the request payload to update a vehicle's status.
 */
public record UpdateVehicleStatusResource(
    @NotBlank(message = "Status is required")
    @Pattern(regexp = "^(?i)(ACTIVE|SOLD|RESERVED)$", message = "Status must be ACTIVE, SOLD, or RESERVED")
    String status
) {}
