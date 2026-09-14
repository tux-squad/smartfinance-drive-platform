package com.smartfinance.smartfinancedriveplatform.billing.domain.model.commands;

/**
 * Command representing a request to pay a pending billing invoice.
 */
public record PayInvoiceCommand(
        Long invoiceId,
        String userId
) {
    public PayInvoiceCommand {
        if (invoiceId == null || invoiceId <= 0) {
            throw new IllegalArgumentException("invoiceId must be greater than zero");
        }
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId must not be blank");
        }
    }
}
