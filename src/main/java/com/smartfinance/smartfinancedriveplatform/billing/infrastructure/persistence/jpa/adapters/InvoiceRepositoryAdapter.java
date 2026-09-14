package com.smartfinance.smartfinancedriveplatform.billing.infrastructure.persistence.jpa.adapters;

import com.smartfinance.smartfinancedriveplatform.billing.domain.model.aggregates.Invoice;
import com.smartfinance.smartfinancedriveplatform.billing.domain.repositories.InvoiceRepository;
import com.smartfinance.smartfinancedriveplatform.billing.infrastructure.persistence.jpa.repositories.InvoiceJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class InvoiceRepositoryAdapter implements InvoiceRepository {

    private final InvoiceJpaRepository invoiceJpaRepository;

    public InvoiceRepositoryAdapter(InvoiceJpaRepository invoiceJpaRepository) {
        this.invoiceJpaRepository = invoiceJpaRepository;
    }

    @Override
    public Invoice save(Invoice invoice) {
        return invoiceJpaRepository.save(invoice);
    }

    @Override
    public Optional<Invoice> findById(Long id) {
        return invoiceJpaRepository.findById(id);
    }

    @Override
    public List<Invoice> findAllByUserId(String userId) {
        return invoiceJpaRepository.findByUserId(userId);
    }

    @Override
    public List<Invoice> findAllBySubscriptionId(Long subscriptionId) {
        return invoiceJpaRepository.findBySubscriptionId(subscriptionId);
    }
}
