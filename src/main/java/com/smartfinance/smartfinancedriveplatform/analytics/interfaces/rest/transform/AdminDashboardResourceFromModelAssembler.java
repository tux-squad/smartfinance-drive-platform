package com.smartfinance.smartfinancedriveplatform.analytics.interfaces.rest.transform;

import com.smartfinance.smartfinancedriveplatform.analytics.domain.model.valueobjects.AdminDashboardMetrics;
import com.smartfinance.smartfinancedriveplatform.analytics.interfaces.rest.resources.AdminDashboardResource;

public final class AdminDashboardResourceFromModelAssembler {

    private AdminDashboardResourceFromModelAssembler() {}

    public static AdminDashboardResource toResource(AdminDashboardMetrics model) {
        if (model == null) return null;

        return new AdminDashboardResource(
                model.totalDealerships(),
                model.activeDealerships(),
                model.totalFinancialEntities(),
                model.totalRegisteredUsers(),
                model.totalVehiclesListed(),
                model.totalCreditApplications(),
                model.totalSimulationsRun(),
                model.totalActiveSubscriptions(),
                model.estimatedMonthlyRecurringRevenueUsd()
        );
    }
}
