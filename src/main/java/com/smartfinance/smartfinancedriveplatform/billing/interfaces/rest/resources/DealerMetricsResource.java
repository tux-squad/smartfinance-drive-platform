package com.smartfinance.smartfinancedriveplatform.billing.interfaces.rest.resources;

public record DealerMetricsResource(
        int totalLeadsGenerated,
        double conversionRate,
        int totalVehicleViews,
        String membershipRoi,
        int activeListingsCount,
        String period
) {
}
