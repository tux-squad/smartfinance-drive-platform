package com.smartfinance.smartfinancedriveplatform.financing.infrastructure.persistence.jpa.entities;

import com.smartfinance.smartfinancedriveplatform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * JPA entity representing 'credit_applications' table.
 */
@Entity
@Table(name = "credit_applications", indexes = {
        @Index(name = "idx_credit_app_applicant", columnList = "applicant_user_id"),
        @Index(name = "idx_credit_app_financial_entity", columnList = "financial_entity_id")
})
@Getter
@Setter
@NoArgsConstructor
public class CreditApplicationPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Column(name = "applicant_user_id", nullable = false)
    private String applicantUserId;

    @Column(name = "vehicle_id", nullable = false)
    private UUID vehicleId;

    @Column(name = "financial_entity_id", nullable = false)
    private UUID financialEntityId;

    @Column(name = "simulation_id")
    private UUID simulationId;

    @Column(name = "requested_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal requestedAmount;

    @Column(name = "down_payment", precision = 12, scale = 2)
    private BigDecimal downPayment;

    @Column(name = "term_months", nullable = false)
    private int termMonths;

    @Column(name = "monthly_income", nullable = false, precision = 12, scale = 2)
    private BigDecimal monthlyIncome;

    @Column(name = "currency", nullable = false, length = 10)
    private String currency;

    @Column(name = "employment_status", nullable = false, length = 50)
    private String employmentStatus;

    @Column(name = "status", nullable = false, length = 30)
    private String status = "PENDING";

    @Column(name = "notes", length = 1000)
    private String notes;
}
