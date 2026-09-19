package com.smartfinance.smartfinancedriveplatform.consultations.infrastructure.persistence.jpa.adapters;

import com.smartfinance.smartfinancedriveplatform.consultations.domain.model.aggregates.AiConsultation;
import com.smartfinance.smartfinancedriveplatform.consultations.domain.model.valueobjects.AiConsultationId;
import com.smartfinance.smartfinancedriveplatform.consultations.domain.repositories.AiConsultationRepository;
import com.smartfinance.smartfinancedriveplatform.consultations.infrastructure.persistence.jpa.assemblers.AiConsultationPersistenceAssembler;
import com.smartfinance.smartfinancedriveplatform.consultations.infrastructure.persistence.jpa.entities.AiConsultationPersistenceEntity;
import com.smartfinance.smartfinancedriveplatform.consultations.infrastructure.persistence.jpa.repositories.SpringDataAiConsultationRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class AiConsultationRepositoryAdapter implements AiConsultationRepository {

    private final SpringDataAiConsultationRepository repository;

    public AiConsultationRepositoryAdapter(SpringDataAiConsultationRepository repository) {
        this.repository = repository;
    }

    @Override
    public AiConsultation save(AiConsultation aiConsultation) {
        AiConsultationPersistenceEntity existing = repository.findById(aiConsultation.getId().value()).orElse(null);
        AiConsultationPersistenceEntity entityToSave = AiConsultationPersistenceAssembler.toEntity(aiConsultation, existing);
        AiConsultationPersistenceEntity saved = repository.save(entityToSave);
        return AiConsultationPersistenceAssembler.toDomain(saved);
    }

    @Override
    public Optional<AiConsultation> findById(AiConsultationId id) {
        return repository.findById(id.value()).map(AiConsultationPersistenceAssembler::toDomain);
    }

    @Override
    public List<AiConsultation> findAllByUserIdOrderByCreatedAtDesc(String userId) {
        return repository.findAllByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(AiConsultationPersistenceAssembler::toDomain)
                .collect(Collectors.toList());
    }
}
