package com.smartfinance.smartfinancedriveplatform.analytics.domain.model.valueobjects;

/**
 * Value Object representing test drive scheduling metrics for a dealership.
 */
public record DealerTestDriveMetrics(
    int totalTestDrives,
    int pendingTestDrives,
    int confirmedTestDrives,
    int completedTestDrives,
    int cancelledTestDrives
) {}
