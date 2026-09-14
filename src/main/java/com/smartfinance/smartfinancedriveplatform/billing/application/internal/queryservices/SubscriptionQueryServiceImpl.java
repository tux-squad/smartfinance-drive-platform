package com.smartfinance.smartfinancedriveplatform.billing.application.internal.queryservices;

import com.smartfinance.smartfinancedriveplatform.billing.domain.model.aggregates.Invoice;
import com.smartfinance.smartfinancedriveplatform.billing.domain.model.aggregates.Subscription;
import com.smartfinance.smartfinancedriveplatform.billing.domain.model.queries.GetInvoicesByUserIdQuery;
import com.smartfinance.smartfinancedriveplatform.billing.domain.model.queries.GetSubscriptionByUserIdQuery;
import com.smartfinance.smartfinancedriveplatform.billing.domain.model.valueobjects.SubscriptionStatus;
import com.smartfinance.smartfinancedriveplatform.billing.domain.repositories.InvoiceRepository;
import com.smartfinance.smartfinancedriveplatform.billing.domain.repositories.SubscriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class SubscriptionQueryServiceImpl implements SubscriptionQueryService {

    private final SubscriptionRepository subscriptionRepository;
    private final InvoiceRepository invoiceRepository;

    public SubscriptionQueryServiceImpl(SubscriptionRepository subscriptionRepository, InvoiceRepository invoiceRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.invoiceRepository = invoiceRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Subscription> handle(GetSubscriptionByUserIdQuery query) {
        return subscriptionRepository.findFirstByUserIdAndStatusOrderByEndDateDesc(query.userId(), SubscriptionStatus.ACTIVE);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Invoice> handle(GetInvoicesByUserIdQuery query) {
        return invoiceRepository.findAllByUserId(query.userId());
    }
}
