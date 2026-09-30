package com.smartfinance.smartfinancedriveplatform.analytics.interfaces.rest.resources;

public record DealerTestDriveMetricsResource(
        int totalTestDrives,
        int pendingTestDrives,
        int confirmedTestDrives,
        int completedTestDrives,
        int cancelledTestDrives
) {}
