package com.smartfinance.smartfinancedriveplatform.billing.infrastructure.persistence.jpa.repositories;

import com.smartfinance.smartfinancedriveplatform.billing.domain.model.aggregates.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvoiceJpaRepository extends JpaRepository<Invoice, Long> {
    List<Invoice> findByUserId(String userId);
    List<Invoice> findBySubscriptionId(Long subscriptionId);
}
