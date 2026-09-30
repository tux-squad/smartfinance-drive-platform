package com.smartfinance.smartfinancedriveplatform.analytics.interfaces.rest.resources;

public record DealerCrmMetricsResource(
        int totalLeads,
        int newLeads,
        int contactedLeads,
        int qualifiedLeads,
        int inNegotiationLeads,
        int closedWonLeads,
        int closedLostLeads,
        double conversionRate
) {}
