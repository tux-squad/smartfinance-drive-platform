package com.smartfinance.smartfinancedriveplatform.analytics.domain.model.queries;

import java.util.UUID;

/**
 * Query to request consolidated dashboard metrics for a partner financial institution.
 */
public record GetFinancialInstitutionDashboardMetricsQuery(
    UUID financialEntityId
) {}
