package com.smartfinance.smartfinancedriveplatform.billing.domain.model.aggregates;

import com.smartfinance.smartfinancedriveplatform.billing.domain.model.valueobjects.InvoiceStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Aggregate Root representing a Billing Invoice or receipt for a Subscription.
 */
@Entity
@Table(name = "invoices", indexes = {
        @Index(name = "idx_invoice_user_id", columnList = "user_id"),
        @Index(name = "idx_invoice_subscription_id", columnList = "subscription_id")
})
@Getter
@Setter
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "subscription_id", nullable = false)
    private Long subscriptionId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InvoiceStatus status;

    @Column(nullable = false)
    private LocalDateTime issuedAt;

    @Column(nullable = false)
    private LocalDateTime dueDate;

    private LocalDateTime paidAt;

    public Invoice() {}

    public Invoice(Long subscriptionId, String userId, BigDecimal amount, String currency) {
        this.subscriptionId = subscriptionId;
        this.userId = userId;
        this.amount = amount;
        this.currency = currency != null ? currency : "USD";
        this.status = InvoiceStatus.PENDING;
        this.issuedAt = LocalDateTime.now();
        this.dueDate = this.issuedAt.plusDays(7);
    }

    public void markPaid() {
        this.status = InvoiceStatus.PAID;
        this.paidAt = LocalDateTime.now();
    }
}
