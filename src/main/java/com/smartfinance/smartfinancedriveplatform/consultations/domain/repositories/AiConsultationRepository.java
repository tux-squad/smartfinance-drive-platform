package com.smartfinance.smartfinancedriveplatform.consultations.domain.repositories;

import com.smartfinance.smartfinancedriveplatform.consultations.domain.model.aggregates.AiConsultation;
import com.smartfinance.smartfinancedriveplatform.consultations.domain.model.valueobjects.AiConsultationId;

import java.util.List;
import java.util.Optional;

public interface AiConsultationRepository {
    AiConsultation save(AiConsultation aiConsultation);
    Optional<AiConsultation> findById(AiConsultationId id);
    List<AiConsultation> findAllByUserIdOrderByCreatedAtDesc(String userId);
}
