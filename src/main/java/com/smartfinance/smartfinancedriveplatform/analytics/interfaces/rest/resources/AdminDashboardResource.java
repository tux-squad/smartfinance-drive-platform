package com.smartfinance.smartfinancedriveplatform.analytics.interfaces.rest.resources;

import java.math.BigDecimal;

public record AdminDashboardResource(
        int totalDealerships,
        int activeDealerships,
        int totalFinancialEntities,
        int totalRegisteredUsers,
        int totalVehiclesListed,
        int totalCreditApplications,
        int totalSimulationsRun,
        int totalActiveSubscriptions,
        BigDecimal estimatedMonthlyRecurringRevenueUsd
) {}
