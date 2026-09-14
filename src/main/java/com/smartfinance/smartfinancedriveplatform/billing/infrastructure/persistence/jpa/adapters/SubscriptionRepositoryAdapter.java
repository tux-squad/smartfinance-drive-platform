package com.smartfinance.smartfinancedriveplatform.billing.infrastructure.persistence.jpa.adapters;

import com.smartfinance.smartfinancedriveplatform.billing.domain.model.aggregates.Subscription;
import com.smartfinance.smartfinancedriveplatform.billing.domain.model.valueobjects.SubscriptionStatus;
import com.smartfinance.smartfinancedriveplatform.billing.domain.repositories.SubscriptionRepository;
import com.smartfinance.smartfinancedriveplatform.billing.infrastructure.persistence.jpa.repositories.SubscriptionJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class SubscriptionRepositoryAdapter implements SubscriptionRepository {

    private final SubscriptionJpaRepository subscriptionJpaRepository;

    public SubscriptionRepositoryAdapter(SubscriptionJpaRepository subscriptionJpaRepository) {
        this.subscriptionJpaRepository = subscriptionJpaRepository;
    }

    @Override
    public Subscription save(Subscription subscription) {
        return subscriptionJpaRepository.save(subscription);
    }

    @Override
    public Optional<Subscription> findById(Long id) {
        return subscriptionJpaRepository.findById(id);
    }

    @Override
    public Optional<Subscription> findFirstByUserIdAndStatusOrderByEndDateDesc(String userId, SubscriptionStatus status) {
        return subscriptionJpaRepository.findFirstByUserIdAndStatusOrderByEndDateDesc(userId, status);
    }

    @Override
    public List<Subscription> findAllByUserId(String userId) {
        return subscriptionJpaRepository.findByUserId(userId);
    }

    @Override
    public Optional<Subscription> findByStripeSubscriptionId(String stripeSubscriptionId) {
        return subscriptionJpaRepository.findByStripeSubscriptionId(stripeSubscriptionId);
    }

    @Override
    public Optional<Subscription> findByStripeCustomerId(String stripeCustomerId) {
        return subscriptionJpaRepository.findByStripeCustomerId(stripeCustomerId);
    }
}
