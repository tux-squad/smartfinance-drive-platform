package com.smartfinance.smartfinancedriveplatform.analytics.interfaces.rest.resources;

public record DealerDashboardResource(
        String dealerUserId,
        DealerInventoryMetricsResource inventory,
        DealerCrmMetricsResource crm,
        DealerTestDriveMetricsResource testDrives,
        DealerFinancingMetricsResource financing,
        int estimatedVehicleViews,
        String membershipRoi,
        String period
) {}
