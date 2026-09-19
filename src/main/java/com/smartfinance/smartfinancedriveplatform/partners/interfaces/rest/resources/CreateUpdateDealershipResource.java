package com.smartfinance.smartfinancedriveplatform.partners.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Resource DTO for creating or updating a Dealership profile.
 */
public record CreateUpdateDealershipResource(
    @NotBlank(message = "RUC is required")
    @Pattern(regexp = "^\\d{11}$", message = "RUC must be 11 digits")
    String ruc,

    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name cannot exceed 255 characters")
    String name,

    @NotBlank(message = "Address is required")
    @Size(max = 500, message = "Address cannot exceed 500 characters")
    String address,

    String phone,

    String email,

    String website,

    String description,

    String operatingHours,

    String logoUrl,

    String bannerUrl
) {}
