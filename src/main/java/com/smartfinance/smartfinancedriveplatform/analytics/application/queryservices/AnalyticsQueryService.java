package com.smartfinance.smartfinancedriveplatform.analytics.application.queryservices;

import com.smartfinance.smartfinancedriveplatform.analytics.domain.model.queries.GetAdminDashboardMetricsQuery;
import com.smartfinance.smartfinancedriveplatform.analytics.domain.model.queries.GetDealerDashboardMetricsQuery;
import com.smartfinance.smartfinancedriveplatform.analytics.domain.model.queries.GetFinancialInstitutionDashboardMetricsQuery;
import com.smartfinance.smartfinancedriveplatform.analytics.domain.model.valueobjects.AdminDashboardMetrics;
import com.smartfinance.smartfinancedriveplatform.analytics.domain.model.valueobjects.DealerDashboardMetrics;
import com.smartfinance.smartfinancedriveplatform.analytics.domain.model.valueobjects.FinancialInstitutionDashboardMetrics;

import java.util.Optional;

/**
 * Application service interface for retrieving aggregated metrics across platform domains.
 */
public interface AnalyticsQueryService {
    DealerDashboardMetrics handle(GetDealerDashboardMetricsQuery query);
    Optional<FinancialInstitutionDashboardMetrics> handle(GetFinancialInstitutionDashboardMetricsQuery query);
    AdminDashboardMetrics handle(GetAdminDashboardMetricsQuery query);
}
