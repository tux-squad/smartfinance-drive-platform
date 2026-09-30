package com.smartfinance.smartfinancedriveplatform.iam.infrastructure.persistence.jpa.adapters;

import com.smartfinance.smartfinancedriveplatform.iam.domain.model.aggregates.CorporateVerificationSession;
import com.smartfinance.smartfinancedriveplatform.iam.domain.repositories.CorporateVerificationSessionRepository;
import com.smartfinance.smartfinancedriveplatform.iam.infrastructure.persistence.jpa.assemblers.CorporateVerificationSessionPersistenceAssembler;
import com.smartfinance.smartfinancedriveplatform.iam.infrastructure.persistence.jpa.entities.CorporateVerificationSessionPersistenceEntity;
import com.smartfinance.smartfinancedriveplatform.iam.infrastructure.persistence.jpa.repositories.SpringDataCorporateVerificationSessionRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Adapter implementing CorporateVerificationSessionRepository delegating to Spring Data JPA.
 */
@Component
public class CorporateVerificationSessionRepositoryAdapter implements CorporateVerificationSessionRepository {

    private final SpringDataCorporateVerificationSessionRepository repository;

    public CorporateVerificationSessionRepositoryAdapter(SpringDataCorporateVerificationSessionRepository repository) {
        this.repository = repository;
    }

    @Override
    public CorporateVerificationSession save(CorporateVerificationSession session) {
        CorporateVerificationSessionPersistenceEntity existing = repository.findById(session.getId()).orElse(null);
        CorporateVerificationSessionPersistenceEntity entityToSave = CorporateVerificationSessionPersistenceAssembler.toEntity(session, existing);
        CorporateVerificationSessionPersistenceEntity saved = repository.save(entityToSave);
        return CorporateVerificationSessionPersistenceAssembler.toDomain(saved);
    }

    @Override
    public Optional<CorporateVerificationSession> findById(UUID id) {
        return repository.findById(id).map(CorporateVerificationSessionPersistenceAssembler::toDomain);
    }

    @Override
    public Optional<CorporateVerificationSession> findLatestActiveSession(String userId, String ruc) {
        return repository.findLatestSession(userId, ruc).map(CorporateVerificationSessionPersistenceAssembler::toDomain);
    }

    @Override
    public long countRecentSessionsByUserId(String userId, java.time.Instant since) {
        return repository.countRecentSessionsByUserId(userId, since);
    }

    @Override
    public void expirePendingSessions(String userId, String ruc) {
        repository.expirePendingSessions(userId, ruc);
    }

    @Override
    public void deleteExpiredSessionsBefore(java.time.Instant threshold) {
        repository.deleteExpiredSessionsBefore(threshold);
    }
}
