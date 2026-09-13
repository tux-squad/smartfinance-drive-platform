package com.smartfinance.smartfinancedriveplatform.financing.infrastructure.persistence.jpa.entities;

import com.smartfinance.smartfinancedriveplatform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity representing the 'simulations' table in the database.
 */
@Entity
@Table(name = "simulations", indexes = {
        @Index(name = "idx_simulations_user_id", columnList = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
public class SimulationPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "vehicle_id", nullable = false)
    private String vehicleId;

    @Column(name = "financial_entity_id", nullable = false)
    private String financialEntityId;

    @Column(name = "vehicle_price_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal vehiclePriceAmount;

    @Column(name = "currency", nullable = false, length = 10)
    private String currency;

    @Column(name = "down_payment_percentage", nullable = false, precision = 7, scale = 4)
    private BigDecimal downPaymentPercentage;

    @Column(name = "down_payment_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal downPaymentAmount;

    @Column(name = "financed_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal financedAmount;

    @Column(name = "balloon_payment_percentage", nullable = false, precision = 7, scale = 4)
    private BigDecimal balloonPaymentPercentage;

    @Column(name = "balloon_payment_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal balloonPaymentAmount;

    @Column(name = "annual_effective_rate", nullable = false, precision = 7, scale = 4)
    private BigDecimal annualEffectiveRate;

    @Column(name = "monthly_credit_life_insurance_rate", precision = 7, scale = 4)
    private BigDecimal monthlyCreditLifeInsuranceRate;

    @Column(name = "vehicle_insurance_fee_amount", precision = 12, scale = 2)
    private BigDecimal vehicleInsuranceFeeAmount;

    @Column(name = "vehicle_insurance_type", length = 20)
    private String vehicleInsuranceType;

    @Column(name = "loan_term_months", nullable = false)
    private int loanTermMonths;

    @Column(name = "grace_period_type", nullable = false, length = 20)
    private String gracePeriodType;

    @Column(name = "grace_period_months", nullable = false)
    private int gracePeriodMonths;

    @Column(name = "initial_fees_amount", precision = 12, scale = 2)
    private BigDecimal initialFeesAmount;

    @Column(name = "discount_rate", precision = 7, scale = 4)
    private BigDecimal discountRate;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "tcea", nullable = false, precision = 7, scale = 4)
    private BigDecimal tcea;

    @Column(name = "tir", nullable = false, precision = 7, scale = 4)
    private BigDecimal tir;

    @Column(name = "van", nullable = false, precision = 12, scale = 2)
    private BigDecimal van;

    @Column(name = "total_interest", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalInterest;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @OneToMany(mappedBy = "simulation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PaymentPeriodPersistenceEntity> paymentPeriods = new ArrayList<>();

    public void addPaymentPeriod(PaymentPeriodPersistenceEntity period) {
        period.setSimulation(this);
        this.paymentPeriods.add(period);
    }
}
