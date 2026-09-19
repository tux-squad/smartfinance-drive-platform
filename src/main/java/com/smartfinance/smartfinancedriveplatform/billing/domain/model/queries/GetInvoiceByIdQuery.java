package com.smartfinance.smartfinancedriveplatform.billing.domain.model.queries;

public record GetInvoiceByIdQuery(Long invoiceId) {
    public GetInvoiceByIdQuery {
        if (invoiceId == null) {
            throw new IllegalArgumentException("invoiceId cannot be null");
        }
    }
}
