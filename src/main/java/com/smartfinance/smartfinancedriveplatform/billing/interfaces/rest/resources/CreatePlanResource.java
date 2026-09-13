package com.smartfinance.smartfinancedriveplatform.billing.interfaces.rest.resources;

import com.smartfinance.smartfinancedriveplatform.billing.domain.model.valueobjects.BillingCycle;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreatePlanResource(
        @NotNull @NotBlank @Size(min = 2, max = 100) String name,
        String description,
        @NotNull @DecimalMin("0.0") BigDecimal price,
        @Size(min = 3, max = 3) String currency,
        BillingCycle billingCycle,
        @Min(1) Integer maxVehicleListings,
        @Min(1) Integer maxSimulationsPerMonth,
        String stripePriceId
) {}
