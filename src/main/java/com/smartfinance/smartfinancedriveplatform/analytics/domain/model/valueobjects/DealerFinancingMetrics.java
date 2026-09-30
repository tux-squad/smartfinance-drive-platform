package com.smartfinance.smartfinancedriveplatform.analytics.domain.model.valueobjects;

/**
 * Value Object representing incoming credit applications metrics for a dealership's inventory.
 */
public record DealerFinancingMetrics(
    int totalApplicationsReceived,
    int pendingApplications,
    int approvedApplications,
    int rejectedApplications
) {}
