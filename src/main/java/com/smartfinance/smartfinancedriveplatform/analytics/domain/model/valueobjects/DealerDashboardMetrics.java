package com.smartfinance.smartfinancedriveplatform.analytics.domain.model.valueobjects;

/**
 * Value Object representing the consolidated dashboard metrics for an authenticated dealership.
 */
public record DealerDashboardMetrics(
    String dealerUserId,
    DealerInventoryMetrics inventory,
    DealerCrmMetrics crm,
    DealerTestDriveMetrics testDrives,
    DealerFinancingMetrics financing,
    int estimatedVehicleViews,
    String membershipRoi,
    String period
) {}
