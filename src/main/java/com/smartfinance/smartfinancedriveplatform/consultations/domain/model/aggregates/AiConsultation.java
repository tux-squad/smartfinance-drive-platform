package com.smartfinance.smartfinancedriveplatform.consultations.domain.model.aggregates;

import com.smartfinance.smartfinancedriveplatform.consultations.domain.model.valueobjects.AiConsultationId;
import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.valueobjects.Money;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

/**
 * AiConsultation Aggregate Root.
 * Represents an AI financial advisor consultation query and generated recommendation.
 */
@Getter
public class AiConsultation extends AbstractDomainAggregateRoot<AiConsultation> {

    private final AiConsultationId id;
    private String userId;
    private String prompt;
    private Money monthlyIncome;
    private Money maxBudget;
    private String recommendationText;
    private String recommendedVehicleCategory;
    private Double estimatedMaxMonthlyFee;
    private Instant createdAt;

    public AiConsultation(AiConsultationId id, String userId, String prompt, Money monthlyIncome, 
                          Money maxBudget, String recommendationText, String recommendedVehicleCategory, 
                          Double estimatedMaxMonthlyFee, Instant createdAt) {
        this.id = id;
        setUserId(userId);
        setPrompt(prompt);
        this.monthlyIncome = monthlyIncome;
        this.maxBudget = maxBudget;
        this.recommendationText = recommendationText;
        this.recommendedVehicleCategory = recommendedVehicleCategory;
        this.estimatedMaxMonthlyFee = estimatedMaxMonthlyFee;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public AiConsultation(String userId, String prompt, Money monthlyIncome, Money maxBudget,
                          String recommendationText, String recommendedVehicleCategory, Double estimatedMaxMonthlyFee) {
        this(new AiConsultationId(UUID.randomUUID()), userId, prompt, monthlyIncome, maxBudget,
             recommendationText, recommendedVehicleCategory, estimatedMaxMonthlyFee, Instant.now());
    }

    public void setUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new DomainValidationException("consultations.error.userId.required");
        }
        this.userId = userId.trim();
    }

    public void setPrompt(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            throw new DomainValidationException("consultations.error.prompt.required");
        }
        this.prompt = prompt.trim();
    }
}
