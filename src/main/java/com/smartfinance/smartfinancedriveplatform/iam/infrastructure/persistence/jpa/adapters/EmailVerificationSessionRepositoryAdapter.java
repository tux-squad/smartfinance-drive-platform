package com.smartfinance.smartfinancedriveplatform.iam.infrastructure.persistence.jpa.adapters;

import com.smartfinance.smartfinancedriveplatform.iam.domain.model.aggregates.EmailVerificationSession;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.EmailVerificationStatus;
import com.smartfinance.smartfinancedriveplatform.iam.domain.repositories.EmailVerificationSessionRepository;
import com.smartfinance.smartfinancedriveplatform.iam.infrastructure.persistence.jpa.assemblers.EmailVerificationSessionPersistenceAssembler;
import com.smartfinance.smartfinancedriveplatform.iam.infrastructure.persistence.jpa.entities.EmailVerificationSessionPersistenceEntity;
import com.smartfinance.smartfinancedriveplatform.iam.infrastructure.persistence.jpa.repositories.SpringDataEmailVerificationSessionRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

/**
 * JPA adapter implementing the domain EmailVerificationSessionRepository interface.
 */
@Repository
public class EmailVerificationSessionRepositoryAdapter implements EmailVerificationSessionRepository {

    private final SpringDataEmailVerificationSessionRepository springDataRepository;

    public EmailVerificationSessionRepositoryAdapter(SpringDataEmailVerificationSessionRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public EmailVerificationSession save(EmailVerificationSession session) {
        EmailVerificationSessionPersistenceEntity entity = EmailVerificationSessionPersistenceAssembler.toEntity(session);
        EmailVerificationSessionPersistenceEntity saved = springDataRepository.save(entity);
        return EmailVerificationSessionPersistenceAssembler.toDomain(saved);
    }

    @Override
    public Optional<EmailVerificationSession> findLatestActiveSession(String email) {
        if (email == null || email.isBlank()) {
            return Optional.empty();
        }
        return springDataRepository.findFirstByEmailAndStatusAndExpiresAtAfterOrderByCreatedAtDesc(
                email.trim(),
                EmailVerificationStatus.PENDING,
                Instant.now()
        ).map(EmailVerificationSessionPersistenceAssembler::toDomain);
    }

    @Override
    public Optional<EmailVerificationSession> findByVerificationToken(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        return springDataRepository.findByVerificationToken(token.trim())
                .map(EmailVerificationSessionPersistenceAssembler::toDomain);
    }

    @Override
    public void expirePendingSessions(String email) {
        if (email != null && !email.isBlank()) {
            springDataRepository.expirePendingSessionsByEmail(email.trim());
        }
    }

    @Override
    public long countRecentSessionsByEmail(String email, Instant since) {
        if (email == null || email.isBlank()) {
            return 0L;
        }
        return springDataRepository.countRecentSessionsByEmail(email.trim(), since);
    }
}
