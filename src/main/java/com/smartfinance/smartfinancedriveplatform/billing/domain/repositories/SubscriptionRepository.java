package com.smartfinance.smartfinancedriveplatform.billing.domain.repositories;

import com.smartfinance.smartfinancedriveplatform.billing.domain.model.aggregates.Subscription;
import com.smartfinance.smartfinancedriveplatform.billing.domain.model.valueobjects.SubscriptionStatus;

import java.util.List;
import java.util.Optional;

/**
 * Domain Repository interface for managing {@link Subscription} aggregates.
 */
public interface SubscriptionRepository {
    Subscription save(Subscription subscription);
    Optional<Subscription> findById(Long id);
    Optional<Subscription> findFirstByUserIdAndStatusOrderByEndDateDesc(String userId, SubscriptionStatus status);
    List<Subscription> findAllByUserId(String userId);
}
