package com.smartfinance.smartfinancedriveplatform.iam.infrastructure.persistence.jpa.adapters;

import com.smartfinance.smartfinancedriveplatform.iam.domain.model.aggregates.PhoneVerificationSession;
import com.smartfinance.smartfinancedriveplatform.iam.domain.repositories.PhoneVerificationSessionRepository;
import com.smartfinance.smartfinancedriveplatform.iam.infrastructure.persistence.jpa.assemblers.PhoneVerificationSessionPersistenceAssembler;
import com.smartfinance.smartfinancedriveplatform.iam.infrastructure.persistence.jpa.entities.PhoneVerificationSessionPersistenceEntity;
import com.smartfinance.smartfinancedriveplatform.iam.infrastructure.persistence.jpa.repositories.SpringDataPhoneVerificationSessionRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter implementing PhoneVerificationSessionRepository delegating to Spring Data JPA.
 */
@Component
public class PhoneVerificationSessionRepositoryAdapter implements PhoneVerificationSessionRepository {

    private final SpringDataPhoneVerificationSessionRepository repository;

    public PhoneVerificationSessionRepositoryAdapter(SpringDataPhoneVerificationSessionRepository repository) {
        this.repository = repository;
    }

    @Override
    public PhoneVerificationSession save(PhoneVerificationSession session) {
        PhoneVerificationSessionPersistenceEntity existing = repository.findById(session.getId()).orElse(null);
        PhoneVerificationSessionPersistenceEntity entityToSave = PhoneVerificationSessionPersistenceAssembler.toEntity(session, existing);
        PhoneVerificationSessionPersistenceEntity saved = repository.save(entityToSave);
        return PhoneVerificationSessionPersistenceAssembler.toDomain(saved);
    }

    @Override
    public Optional<PhoneVerificationSession> findById(UUID id) {
        return repository.findById(id).map(PhoneVerificationSessionPersistenceAssembler::toDomain);
    }

    @Override
    public Optional<PhoneVerificationSession> findByVerificationToken(String verificationToken) {
        if (verificationToken == null || verificationToken.isBlank()) {
            return Optional.empty();
        }
        return repository.findByVerificationToken(verificationToken.trim())
                .map(PhoneVerificationSessionPersistenceAssembler::toDomain);
    }

    @Override
    public Optional<PhoneVerificationSession> findLatestActiveSession(String phoneNumber) {
        return repository.findLatestSession(phoneNumber).map(PhoneVerificationSessionPersistenceAssembler::toDomain);
    }

    @Override
    public long countRecentSessionsByPhoneNumber(String phoneNumber, Instant since) {
        return repository.countRecentSessionsByPhoneNumber(phoneNumber, since);
    }

    @Override
    public void expirePendingSessions(String phoneNumber) {
        repository.expirePendingSessions(phoneNumber);
    }

    @Override
    public void deleteExpiredSessionsBefore(Instant threshold) {
        repository.deleteExpiredSessionsBefore(threshold);
    }
}
