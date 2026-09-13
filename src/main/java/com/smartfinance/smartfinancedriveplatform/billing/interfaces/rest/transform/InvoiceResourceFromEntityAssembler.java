package com.smartfinance.smartfinancedriveplatform.billing.interfaces.rest.transform;

import com.smartfinance.smartfinancedriveplatform.billing.domain.model.aggregates.Invoice;
import com.smartfinance.smartfinancedriveplatform.billing.interfaces.rest.resources.InvoiceResource;

public class InvoiceResourceFromEntityAssembler {
    public static InvoiceResource toResourceFromEntity(Invoice invoice) {
        if (invoice == null) return null;
        return new InvoiceResource(
                invoice.getId(),
                invoice.getSubscriptionId(),
                invoice.getUserId(),
                invoice.getAmount(),
                invoice.getCurrency(),
                invoice.getStatus(),
                invoice.getIssuedAt(),
                invoice.getDueDate(),
                invoice.getPaidAt()
        );
    }
}
