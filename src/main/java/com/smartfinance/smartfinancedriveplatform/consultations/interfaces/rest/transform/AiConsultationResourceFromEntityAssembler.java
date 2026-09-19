package com.smartfinance.smartfinancedriveplatform.consultations.interfaces.rest.transform;

import com.smartfinance.smartfinancedriveplatform.consultations.domain.model.aggregates.AiConsultation;
import com.smartfinance.smartfinancedriveplatform.consultations.interfaces.rest.resources.AiConsultationResource;

public final class AiConsultationResourceFromEntityAssembler {

    private AiConsultationResourceFromEntityAssembler() {}

    public static AiConsultationResource toResourceFromEntity(AiConsultation domain) {
        String currency = domain.getMonthlyIncome() != null ? domain.getMonthlyIncome().currency() 
                        : (domain.getMaxBudget() != null ? domain.getMaxBudget().currency() : "PEN");
        return new AiConsultationResource(
            domain.getId().value(),
            domain.getUserId(),
            domain.getPrompt(),
            domain.getMonthlyIncome() != null ? domain.getMonthlyIncome().amount() : null,
            domain.getMaxBudget() != null ? domain.getMaxBudget().amount() : null,
            currency,
            domain.getRecommendationText(),
            domain.getRecommendedVehicleCategory(),
            domain.getEstimatedMaxMonthlyFee(),
            domain.getCreatedAt()
        );
    }
}
