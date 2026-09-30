package com.smartfinance.smartfinancedriveplatform.analytics.domain.model.valueobjects;

/**
 * Value Object representing the consolidated factual dashboard metrics for an authenticated dealership.
 */
public record DealerDashboardMetrics(
    String dealerUserId,
    DealerInventoryMetrics inventory,
    DealerCrmMetrics crm,
    DealerTestDriveMetrics testDrives,
    DealerFinancingMetrics financing,
    String period
) {}
