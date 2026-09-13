package com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * Request payload for requesting automatic dealer role upgrade via SUNAT RUC verification.
 */
public record RequestDealerRoleResource(
        @NotNull(message = "RUC is required")
        @NotBlank(message = "RUC must not be blank")
        @Pattern(regexp = "^\\d{11}$", message = "RUC must consist of exactly 11 numeric digits")
        String ruc
) {}
