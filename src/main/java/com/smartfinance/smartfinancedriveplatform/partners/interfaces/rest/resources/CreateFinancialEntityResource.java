package com.smartfinance.smartfinancedriveplatform.partners.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Resource DTO representing the request payload to register a new financial entity.
 */
public record CreateFinancialEntityResource(
    String userId,

    @Pattern(regexp = "^\\d{11}$", message = "RUC must be exactly 11 digits")
    String ruc,

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name cannot exceed 100 characters")
    String name,

    @Pattern(regexp = "^(https?://.+)?$", message = "Logo URL must be a valid HTTP or HTTPS URL")
    @Size(max = 1000, message = "Logo URL cannot exceed 1000 characters")
    String logoUrl,

    @Pattern(regexp = "^(https?://.+)?$", message = "Banner URL must be a valid HTTP or HTTPS URL")
    @Size(max = 1000, message = "Banner URL cannot exceed 1000 characters")
    String bannerUrl
) {
    public CreateFinancialEntityResource(String userId, String name, String logoUrl, String bannerUrl) {
        this(userId, null, name, logoUrl, bannerUrl);
    }

    public CreateFinancialEntityResource(String name, String logoUrl, String bannerUrl) {
        this(null, null, name, logoUrl, bannerUrl);
    }

    public CreateFinancialEntityResource(String name) {
        this(null, null, name, null, null);
    }
}
