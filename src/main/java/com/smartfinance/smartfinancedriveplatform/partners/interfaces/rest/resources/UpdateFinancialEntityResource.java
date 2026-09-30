package com.smartfinance.smartfinancedriveplatform.partners.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Resource DTO representing the request payload to update an existing financial entity.
 */
public record UpdateFinancialEntityResource(
    String userId,

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name cannot exceed 100 characters")
    String name,

    @Size(max = 1000, message = "Logo URL cannot exceed 1000 characters")
    String logoUrl,

    @Size(max = 1000, message = "Banner URL cannot exceed 1000 characters")
    String bannerUrl
) {
    public UpdateFinancialEntityResource(String name, String logoUrl, String bannerUrl) {
        this(null, name, logoUrl, bannerUrl);
    }

    public UpdateFinancialEntityResource(String name) {
        this(null, name, null, null);
    }
}
