package com.smartfinance.smartfinancedriveplatform.analytics.domain.model.valueobjects;

import java.math.BigDecimal;

/**
 * Value Object representing the consolidated platform-wide metrics for super administrators.
 */
public record AdminDashboardMetrics(
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
