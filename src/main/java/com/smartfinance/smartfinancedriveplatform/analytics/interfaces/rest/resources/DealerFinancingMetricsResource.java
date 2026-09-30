package com.smartfinance.smartfinancedriveplatform.analytics.interfaces.rest.resources;

public record DealerFinancingMetricsResource(
        int totalApplicationsReceived,
        int pendingApplications,
        int approvedApplications,
        int rejectedApplications
) {}
