package com.smartfinance.smartfinancedriveplatform.consultations.infrastructure.persistence.jpa.assemblers;

import com.smartfinance.smartfinancedriveplatform.consultations.domain.model.aggregates.AiConsultation;
import com.smartfinance.smartfinancedriveplatform.consultations.domain.model.valueobjects.AiConsultationId;
import com.smartfinance.smartfinancedriveplatform.consultations.infrastructure.persistence.jpa.entities.AiConsultationPersistenceEntity;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.valueobjects.Money;

public final class AiConsultationPersistenceAssembler {

    private AiConsultationPersistenceAssembler() {}

    public static AiConsultationPersistenceEntity toEntity(AiConsultation domain, AiConsultationPersistenceEntity entity) {
        if (entity == null) {
            entity = new AiConsultationPersistenceEntity();
        }
        entity.setId(domain.getId().value());
        entity.setUserId(domain.getUserId());
        entity.setPrompt(domain.getPrompt());
        if (domain.getMonthlyIncome() != null) {
            entity.setMonthlyIncome(domain.getMonthlyIncome().amount());
            entity.setCurrency(domain.getMonthlyIncome().currency());
        }
        if (domain.getMaxBudget() != null) {
            entity.setMaxBudget(domain.getMaxBudget().amount());
            if (entity.getCurrency() == null) {
                entity.setCurrency(domain.getMaxBudget().currency());
            }
        }
        entity.setRecommendationText(domain.getRecommendationText());
        entity.setRecommendedVehicleCategory(domain.getRecommendedVehicleCategory());
        entity.setEstimatedMaxMonthlyFee(domain.getEstimatedMaxMonthlyFee());
        return entity;
    }

    public static AiConsultation toDomain(AiConsultationPersistenceEntity entity) {
        String currency = entity.getCurrency() != null ? entity.getCurrency() : "PEN";
        return new AiConsultation(
            new AiConsultationId(entity.getId()),
            entity.getUserId(),
            entity.getPrompt(),
            entity.getMonthlyIncome() != null ? new Money(entity.getMonthlyIncome(), currency) : null,
            entity.getMaxBudget() != null ? new Money(entity.getMaxBudget(), currency) : null,
            entity.getRecommendationText(),
            entity.getRecommendedVehicleCategory(),
            entity.getEstimatedMaxMonthlyFee(),
            entity.getCreatedAt()
        );
    }
}
