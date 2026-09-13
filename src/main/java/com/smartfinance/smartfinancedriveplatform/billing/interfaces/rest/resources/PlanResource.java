package com.smartfinance.smartfinancedriveplatform.billing.interfaces.rest.resources;

import com.smartfinance.smartfinancedriveplatform.billing.domain.model.valueobjects.BillingCycle;

import java.math.BigDecimal;

public record PlanResource(
        Long id,
        String name,
        String description,
        BigDecimal price,
        String currency,
        BillingCycle billingCycle,
        Integer maxVehicleListings,
        Integer maxSimulationsPerMonth,
        boolean active
) {}
