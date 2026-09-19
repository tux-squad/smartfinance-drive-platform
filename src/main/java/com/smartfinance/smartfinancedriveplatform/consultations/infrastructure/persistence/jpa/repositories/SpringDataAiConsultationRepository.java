package com.smartfinance.smartfinancedriveplatform.consultations.infrastructure.persistence.jpa.repositories;

import com.smartfinance.smartfinancedriveplatform.consultations.infrastructure.persistence.jpa.entities.AiConsultationPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataAiConsultationRepository extends JpaRepository<AiConsultationPersistenceEntity, UUID> {
    List<AiConsultationPersistenceEntity> findAllByUserIdOrderByCreatedAtDesc(String userId);
}
