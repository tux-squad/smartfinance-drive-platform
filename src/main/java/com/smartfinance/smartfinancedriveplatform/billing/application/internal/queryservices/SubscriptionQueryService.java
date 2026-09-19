package com.smartfinance.smartfinancedriveplatform.billing.application.internal.queryservices;

import com.smartfinance.smartfinancedriveplatform.billing.domain.model.aggregates.Invoice;
import com.smartfinance.smartfinancedriveplatform.billing.domain.model.aggregates.Subscription;
import com.smartfinance.smartfinancedriveplatform.billing.domain.model.queries.GetInvoiceByIdQuery;
import com.smartfinance.smartfinancedriveplatform.billing.domain.model.queries.GetInvoicesByUserIdQuery;
import com.smartfinance.smartfinancedriveplatform.billing.domain.model.queries.GetSubscriptionByUserIdQuery;

import java.util.List;
import java.util.Optional;

/**
 * Application Query Service interface for reading subscriptions and invoices.
 */
public interface SubscriptionQueryService {
    Optional<Subscription> handle(GetSubscriptionByUserIdQuery query);
    List<Invoice> handle(GetInvoicesByUserIdQuery query);
    Optional<Invoice> handle(GetInvoiceByIdQuery query);
}
