package com.smartfinance.smartfinancedriveplatform.billing.application.internal.commandservices;

import com.smartfinance.smartfinancedriveplatform.billing.domain.model.aggregates.Invoice;
import com.smartfinance.smartfinancedriveplatform.billing.domain.model.aggregates.Subscription;
import com.smartfinance.smartfinancedriveplatform.billing.domain.model.commands.CancelSubscriptionCommand;
import com.smartfinance.smartfinancedriveplatform.billing.domain.model.commands.PayInvoiceCommand;
import com.smartfinance.smartfinancedriveplatform.billing.domain.model.commands.SubscribeUserCommand;

import java.util.Optional;

/**
 * Application Command Service interface for managing subscriptions and invoices.
 */
public interface SubscriptionCommandService {
    Optional<Subscription> handle(SubscribeUserCommand command);
    Optional<Subscription> handle(CancelSubscriptionCommand command);
    Optional<Invoice> handle(PayInvoiceCommand command);
}
