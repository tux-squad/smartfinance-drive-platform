package com.smartfinance.smartfinancedriveplatform.billing.domain.model.commands;

import com.smartfinance.smartfinancedriveplatform.billing.domain.model.valueobjects.BillingCycle;

import java.math.BigDecimal;

/**
 * Command representing a request to create a new SaaS subscription plan tier.
 */
public record CreatePlanCommand(
        String name,
        String description,
        BigDecimal price,
        String currency,
        BillingCycle billingCycle,
        Integer maxVehicleListings,
        Integer maxSimulationsPerMonth,
        String stripePriceId
) {
    public CreatePlanCommand(String name, String description, BigDecimal price, String currency,
                             BillingCycle billingCycle, Integer maxVehicleListings, Integer maxSimulationsPerMonth) {
        this(name, description, price, currency, billingCycle, maxVehicleListings, maxSimulationsPerMonth, null);
    }

    public CreatePlanCommand {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Plan name must not be blank");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Plan price must not be negative");
        }
    }
}
