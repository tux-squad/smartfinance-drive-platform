package com.smartfinance.smartfinancedriveplatform.partners.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Resource DTO representing the request payload to update an existing financial entity.
 */
public record UpdateFinancialEntityResource(
    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name cannot exceed 100 characters")
    String name
) {}
