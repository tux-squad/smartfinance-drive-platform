package com.smartfinance.smartfinancedriveplatform.billing.domain.model.aggregates;

import com.smartfinance.smartfinancedriveplatform.billing.domain.model.valueobjects.BillingCycle;
import com.smartfinance.smartfinancedriveplatform.billing.domain.model.valueobjects.SubscriptionStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Aggregate Root representing an active or past User Subscription.
 */
@Entity
@Table(name = "subscriptions", indexes = {
        @Index(name = "idx_subscription_user_id", columnList = "user_id")
})
@Getter
@Setter
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SubscriptionStatus status;

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private LocalDateTime endDate;

    @Column(nullable = false)
    private boolean autoRenew = true;

    public Subscription() {}

    public Subscription(String userId, Plan plan, boolean autoRenew) {
        this.userId = userId;
        this.plan = plan;
        this.status = SubscriptionStatus.ACTIVE;
        this.startDate = LocalDateTime.now();
        this.autoRenew = autoRenew;
        if (plan.getBillingCycle() == BillingCycle.ANNUAL) {
            this.endDate = this.startDate.plusYears(1);
        } else {
            this.endDate = this.startDate.plusMonths(1);
        }
    }

    public void cancel() {
        this.status = SubscriptionStatus.CANCELED;
        this.autoRenew = false;
    }

    public boolean isActive() {
        return this.status == SubscriptionStatus.ACTIVE && LocalDateTime.now().isBefore(this.endDate);
    }
}
