package com.smartfinance.smartfinancedriveplatform.billing.infrastructure.persistence.jpa.repositories;

import com.smartfinance.smartfinancedriveplatform.billing.domain.model.aggregates.Subscription;
import com.smartfinance.smartfinancedriveplatform.billing.domain.model.valueobjects.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionJpaRepository extends JpaRepository<Subscription, Long> {
    Optional<Subscription> findFirstByUserIdAndStatusOrderByEndDateDesc(String userId, SubscriptionStatus status);
    List<Subscription> findByUserId(String userId);
}
