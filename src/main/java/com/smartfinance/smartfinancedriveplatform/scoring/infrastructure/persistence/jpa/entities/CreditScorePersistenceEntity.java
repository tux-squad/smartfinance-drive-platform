package com.smartfinance.smartfinancedriveplatform.scoring.infrastructure.persistence.jpa.entities;

import com.smartfinance.smartfinancedriveplatform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

import jakarta.persistence.Index;

/**
 * JPA entity representing the 'credit_scores' table in the database.
 */
@Entity
@Table(name = "credit_scores", indexes = {
        @Index(name = "idx_credit_scores_profile_id", columnList = "profile_id")
})
@Getter
@Setter
@NoArgsConstructor
public class CreditScorePersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Column(name = "profile_id", nullable = false)
    private String profileId;

    @Column(name = "simulation_id")
    private String simulationId;

    @Column(name = "currency", nullable = false, length = 10)
    private String currency;

    @Column(name = "monthly_income_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal monthlyIncomeAmount;

    @Column(name = "projected_monthly_installment_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal projectedMonthlyInstallmentAmount;

    @Column(name = "dti_ratio", nullable = false, precision = 9, scale = 6)
    private BigDecimal dtiRatio;

    @Column(name = "risk_tier", nullable = false, length = 20)
    private String riskTier;

    @Column(name = "rate_adjustment", nullable = false, precision = 9, scale = 6)
    private BigDecimal rateAdjustment;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Column(name = "assessment_notes", length = 500)
    private String assessmentNotes;
}
