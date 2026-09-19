package com.smartfinance.smartfinancedriveplatform.iam.domain.model.queries;

public record GetSalesAgentsForDealerQuery(String dealerUserId) {
    public GetSalesAgentsForDealerQuery {
        if (dealerUserId == null || dealerUserId.isBlank()) {
            throw new IllegalArgumentException("dealerUserId cannot be null or empty");
        }
    }
}
