package com.smartfinance.smartfinancedriveplatform.financing.infrastructure.persistence.jpa.entities;

import com.smartfinance.smartfinancedriveplatform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * JPA entity representing the 'simulation_payment_periods' table in the database.
 */
@Entity
@Table(name = "simulation_payment_periods")
@Getter
@Setter
@NoArgsConstructor
public class PaymentPeriodPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "simulation_id", nullable = false)
    private SimulationPersistenceEntity simulation;

    @Column(name = "period_number", nullable = false)
    private int periodNumber;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "days_in_period", nullable = false)
    private int daysInPeriod;

    @Column(name = "currency", nullable = false, length = 10)
    private String currency;

    @Column(name = "initial_balance_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal initialBalanceAmount;

    @Column(name = "interest_payment_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal interestPaymentAmount;

    @Column(name = "principal_amortization_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal principalAmortizationAmount;

    @Column(name = "credit_life_insurance_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal creditLifeInsuranceAmount;

    @Column(name = "vehicle_insurance_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal vehicleInsuranceAmount;

    @Column(name = "total_installment_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalInstallmentAmount;

    @Column(name = "final_balance_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal finalBalanceAmount;

    @Column(name = "grace_type", nullable = false, length = 20)
    private String graceType;
}
