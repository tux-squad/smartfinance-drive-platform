package com.smartfinance.smartfinancedriveplatform.analytics.domain.model.queries;

import com.smartfinance.smartfinancedriveplatform.analytics.domain.model.valueobjects.MetricPeriod;

/**
 * Query to request consolidated dashboard metrics for an authenticated dealer user.
 */
public record GetDealerDashboardMetricsQuery(
    String dealerUserId,
    MetricPeriod period
) {
    public GetDealerDashboardMetricsQuery(String dealerUserId) {
        this(dealerUserId, MetricPeriod.LAST_30_DAYS);
    }
}
