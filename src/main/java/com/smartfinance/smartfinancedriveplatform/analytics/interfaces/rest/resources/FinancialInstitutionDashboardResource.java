package com.smartfinance.smartfinancedriveplatform.analytics.interfaces.rest.resources;

import java.math.BigDecimal;
import java.util.UUID;

public record FinancialInstitutionDashboardResource(
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
        int activeRateBenchmarksCount
) {}
