package com.smartfinance.smartfinancedriveplatform.scoring.infrastructure.persistence.jpa.adapters;

import com.smartfinance.smartfinancedriveplatform.scoring.domain.model.aggregates.CreditScore;
import com.smartfinance.smartfinancedriveplatform.scoring.domain.model.valueobjects.ScoreId;
import com.smartfinance.smartfinancedriveplatform.scoring.domain.repositories.CreditScoreRepository;
import com.smartfinance.smartfinancedriveplatform.scoring.infrastructure.persistence.jpa.assemblers.CreditScorePersistenceAssembler;
import com.smartfinance.smartfinancedriveplatform.scoring.infrastructure.persistence.jpa.entities.CreditScorePersistenceEntity;
import com.smartfinance.smartfinancedriveplatform.scoring.infrastructure.persistence.jpa.repositories.SpringDataCreditScoreRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adapter implementing the CreditScoreRepository domain interface.
 */
@Component
public class CreditScoreRepositoryAdapter implements CreditScoreRepository {

    private final SpringDataCreditScoreRepository springDataCreditScoreRepository;

    public CreditScoreRepositoryAdapter(SpringDataCreditScoreRepository springDataCreditScoreRepository) {
        this.springDataCreditScoreRepository = springDataCreditScoreRepository;
    }

    @Override
    public CreditScore save(CreditScore creditScore) {
        CreditScorePersistenceEntity existingEntity = springDataCreditScoreRepository.findById(creditScore.getId().value()).orElse(null);
        CreditScorePersistenceEntity entityToSave = CreditScorePersistenceAssembler.toEntity(creditScore, existingEntity);
        CreditScorePersistenceEntity savedEntity = springDataCreditScoreRepository.save(entityToSave);
        return CreditScorePersistenceAssembler.toDomain(savedEntity);
    }

    @Override
    public Optional<CreditScore> findById(ScoreId scoreId) {
        return springDataCreditScoreRepository.findById(scoreId.value())
                .map(CreditScorePersistenceAssembler::toDomain);
    }

    @Override
    public List<CreditScore> findByProfileId(String profileId) {
        return springDataCreditScoreRepository.findByProfileId(profileId).stream()
                .map(CreditScorePersistenceAssembler::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<CreditScore> findAll() {
        return springDataCreditScoreRepository.findAll().stream()
                .map(CreditScorePersistenceAssembler::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public org.springframework.data.domain.Page<CreditScore> findAll(org.springframework.data.domain.Pageable pageable) {
        return springDataCreditScoreRepository.findAll(pageable)
                .map(CreditScorePersistenceAssembler::toDomain);
    }

    @Override
    public org.springframework.data.domain.Page<CreditScore> findByProfileId(String profileId, org.springframework.data.domain.Pageable pageable) {
        return springDataCreditScoreRepository.findByProfileId(profileId, pageable)
                .map(CreditScorePersistenceAssembler::toDomain);
    }

    @Override
    public void deleteById(ScoreId scoreId) {
        springDataCreditScoreRepository.deleteById(scoreId.value());
    }

    @Override
    public boolean existsById(ScoreId scoreId) {
        return springDataCreditScoreRepository.existsById(scoreId.value());
    }
}
