package com.smartfinance.smartfinancedriveplatform.billing.interfaces.rest.resources;

import com.smartfinance.smartfinancedriveplatform.billing.domain.model.valueobjects.BillingCycle;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreatePlanResource(
        @NotNull @NotBlank String name,
        String description,
        @NotNull @DecimalMin("0.0") BigDecimal price,
        String currency,
        BillingCycle billingCycle,
        @Min(1) Integer maxVehicleListings,
        @Min(1) Integer maxSimulationsPerMonth
) {}
