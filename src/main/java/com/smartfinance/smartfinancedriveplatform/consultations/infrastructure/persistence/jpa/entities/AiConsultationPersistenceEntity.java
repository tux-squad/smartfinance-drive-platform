package com.smartfinance.smartfinancedriveplatform.consultations.infrastructure.persistence.jpa.entities;

import com.smartfinance.smartfinancedriveplatform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "ai_consultations", indexes = {
        @Index(name = "idx_ai_consultation_user", columnList = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
public class AiConsultationPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "prompt", nullable = false, length = 2000)
    private String prompt;

    @Column(name = "monthly_income", precision = 12, scale = 2)
    private BigDecimal monthlyIncome;

    @Column(name = "max_budget", precision = 12, scale = 2)
    private BigDecimal maxBudget;

    @Column(name = "currency", length = 10)
    private String currency;

    @Column(name = "recommendation_text", length = 4000)
    private String recommendationText;

    @Column(name = "recommended_vehicle_category", length = 100)
    private String recommendedVehicleCategory;

    @Column(name = "estimated_max_monthly_fee")
    private Double estimatedMaxMonthlyFee;
}
