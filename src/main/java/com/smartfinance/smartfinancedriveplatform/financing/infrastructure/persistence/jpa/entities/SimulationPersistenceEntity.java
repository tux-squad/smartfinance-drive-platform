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
@Table(name = "simulations")
@Getter
@Setter
@NoArgsConstructor
public class SimulationPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "vehicle_id")
    private String vehicleId;

    @Column(name = "financial_entity_id")
    private String financialEntityId;

    @Column(name = "currency", nullable = false, length = 10)
    private String currency;

    @Column(name = "vehicle_price_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal vehiclePriceAmount;

    @Column(name = "down_payment_percentage", nullable = false, precision = 9, scale = 6)
    private BigDecimal downPaymentPercentage;

    @Column(name = "balloon_payment_percentage", nullable = false, precision = 9, scale = 6)
    private BigDecimal balloonPaymentPercentage;

    @Column(name = "annual_effective_rate", nullable = false, precision = 9, scale = 6)
    private BigDecimal annualEffectiveRate;

    @Column(name = "monthly_credit_life_insurance_rate", precision = 9, scale = 6)
    private BigDecimal monthlyCreditLifeInsuranceRate;

    @Column(name = "vehicle_insurance_fee_amount", precision = 12, scale = 2)
    private BigDecimal vehicleInsuranceFeeAmount;

    @Column(name = "vehicle_insurance_type", nullable = false, length = 20)
    private String vehicleInsuranceType;

    @Column(name = "loan_term_months", nullable = false)
    private int loanTermMonths;

    @Column(name = "grace_period_type", nullable = false, length = 20)
    private String gracePeriodType;

    @Column(name = "grace_period_months", nullable = false)
    private int gracePeriodMonths;

    @Column(name = "initial_fees_amount", precision = 12, scale = 2)
    private BigDecimal initialFeesAmount;

    @Column(name = "discount_rate", precision = 9, scale = 6)
    private BigDecimal discountRate;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    // Computed Output Persistence Fields
    @Column(name = "financed_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal financedAmount;

    @Column(name = "down_payment_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal downPaymentAmount;

    @Column(name = "balloon_payment_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal balloonPaymentAmount;

    @Column(name = "tcea", nullable = false, precision = 9, scale = 6)
    private BigDecimal tcea;

    @Column(name = "tir", nullable = false, precision = 9, scale = 6)
    private BigDecimal tir;

    @Column(name = "van", nullable = false, precision = 12, scale = 2)
    private BigDecimal van;

    @Column(name = "total_interest", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalInterest;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @OneToMany(mappedBy = "simulation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<PaymentPeriodPersistenceEntity> paymentPeriods = new ArrayList<>();

    public void addPaymentPeriod(PaymentPeriodPersistenceEntity period) {
        period.setSimulation(this);
        this.paymentPeriods.add(period);
    }
}
