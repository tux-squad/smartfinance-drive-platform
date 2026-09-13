package com.smartfinance.smartfinancedriveplatform.billing.domain.model.aggregates;

import com.smartfinance.smartfinancedriveplatform.billing.domain.model.valueobjects.BillingCycle;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Aggregate Root representing a SaaS Subscription Plan tier.
 */
@Entity
@Table(name = "plans")
@Getter
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BillingCycle billingCycle;

    @Column(nullable = false)
    private Integer maxVehicleListings;

    @Column(nullable = false)
    private Integer maxSimulationsPerMonth;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "stripe_price_id")
    private String stripePriceId;

    public Plan() {}

    public Plan(String name, String description, BigDecimal price, String currency,
                BillingCycle billingCycle, Integer maxVehicleListings, Integer maxSimulationsPerMonth) {
        this(name, description, price, currency, billingCycle, maxVehicleListings, maxSimulationsPerMonth, null);
    }

    public Plan(String name, String description, BigDecimal price, String currency,
                BillingCycle billingCycle, Integer maxVehicleListings, Integer maxSimulationsPerMonth,
                String stripePriceId) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.currency = currency != null ? currency : "USD";
        this.billingCycle = billingCycle != null ? billingCycle : BillingCycle.MONTHLY;
        this.maxVehicleListings = maxVehicleListings != null ? maxVehicleListings : 10;
        this.maxSimulationsPerMonth = maxSimulationsPerMonth != null ? maxSimulationsPerMonth : 50;
        this.active = true;
        this.stripePriceId = stripePriceId;
    }

    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }

    public void updateStripePriceId(String stripePriceId) {
        this.stripePriceId = stripePriceId;
    }
}
