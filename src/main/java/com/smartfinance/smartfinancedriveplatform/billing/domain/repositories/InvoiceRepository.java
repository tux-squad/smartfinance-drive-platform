package com.smartfinance.smartfinancedriveplatform.billing.domain.repositories;

import com.smartfinance.smartfinancedriveplatform.billing.domain.model.aggregates.Invoice;

import java.util.List;
import java.util.Optional;

/**
 * Domain Repository interface for managing {@link Invoice} aggregates.
 */
public interface InvoiceRepository {
    Invoice save(Invoice invoice);
    Optional<Invoice> findById(Long id);
    List<Invoice> findAllByUserId(String userId);
    List<Invoice> findAllBySubscriptionId(Long subscriptionId);
}
