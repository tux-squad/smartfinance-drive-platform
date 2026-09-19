package com.smartfinance.smartfinancedriveplatform.consultations.domain.model.valueobjects;

import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import java.util.UUID;

public record AiConsultationId(UUID value) {
    public AiConsultationId {
        if (value == null) {
            throw new DomainValidationException("consultations.error.aiConsultationId.required");
        }
    }
}
