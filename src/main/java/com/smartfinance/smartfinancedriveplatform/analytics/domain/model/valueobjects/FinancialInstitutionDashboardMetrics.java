package com.smartfinance.smartfinancedriveplatform.analytics.domain.model.valueobjects;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Value Object representing the consolidated dashboard metrics for a partner financial institution (bank).
 */
public record FinancialInstitutionDashboardMetrics(
    UUID financialEntityId,
    String financialEntityName,
    int totalApplicationsReceived,
    int underReviewApplications,
    int approvedApplications,
    int rejectedApplications,
    int disbursedApplications,
    double approvalRate,
    BigDecimal totalRequestedVolumePen,
    BigDecimal totalDisbursedVolumePen,
    BigDecimal averageTea,
    int activeRateBenchmarksCount,
    int totalSimulationsCount
) {}
