package com.smartfinance.smartfinancedriveplatform.billing.interfaces.rest.transform;

import com.smartfinance.smartfinancedriveplatform.billing.domain.model.aggregates.Plan;
import com.smartfinance.smartfinancedriveplatform.billing.interfaces.rest.resources.PlanResource;

public class PlanResourceFromEntityAssembler {
    public static PlanResource toResourceFromEntity(Plan plan) {
        if (plan == null) return null;
        return new PlanResource(
                plan.getId(),
                plan.getName(),
                plan.getDescription(),
                plan.getPrice(),
                plan.getCurrency(),
                plan.getBillingCycle(),
                plan.getMaxVehicleListings(),
                plan.getMaxSimulationsPerMonth(),
                plan.isActive(),
                plan.getStripePriceId()
        );
    }
}
