package com.smartfinance.smartfinancedriveplatform.iam.domain.model.queries;

import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.SalesAgentId;

public record GetSalesAgentByIdQuery(SalesAgentId salesAgentId) {
    public GetSalesAgentByIdQuery {
        if (salesAgentId == null) {
            throw new IllegalArgumentException("salesAgentId cannot be null");
        }
    }
}
