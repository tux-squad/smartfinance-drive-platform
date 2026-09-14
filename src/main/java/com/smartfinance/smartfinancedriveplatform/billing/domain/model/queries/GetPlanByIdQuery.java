package com.smartfinance.smartfinancedriveplatform.billing.domain.model.queries;

/**
 * Query to retrieve a specific SaaS subscription plan by ID.
 */
public record GetPlanByIdQuery(Long planId) {
    public GetPlanByIdQuery {
        if (planId == null || planId <= 0) {
            throw new IllegalArgumentException("planId must be greater than zero");
        }
    }
}
