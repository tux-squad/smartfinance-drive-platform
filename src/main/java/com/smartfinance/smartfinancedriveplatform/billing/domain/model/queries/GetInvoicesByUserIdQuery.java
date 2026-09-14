package com.smartfinance.smartfinancedriveplatform.billing.domain.model.queries;

/**
 * Query to retrieve all invoices issued to a user.
 */
public record GetInvoicesByUserIdQuery(String userId) {
    public GetInvoicesByUserIdQuery {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId must not be blank");
        }
    }
}
