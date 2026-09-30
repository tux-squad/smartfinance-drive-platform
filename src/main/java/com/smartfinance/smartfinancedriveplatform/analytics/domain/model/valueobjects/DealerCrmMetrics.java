package com.smartfinance.smartfinancedriveplatform.analytics.domain.model.valueobjects;

/**
 * Value Object representing CRM pipeline and lead conversion metrics for a dealership.
 */
public record DealerCrmMetrics(
    int totalLeads,
    int newLeads,
    int contactedLeads,
    int qualifiedLeads,
    int inNegotiationLeads,
    int closedWonLeads,
    int closedLostLeads,
    double conversionRate
) {}
