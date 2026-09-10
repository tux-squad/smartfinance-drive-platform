package com.smartfinance.smartfinancedriveplatform.scoring.domain.repositories;

import com.smartfinance.smartfinancedriveplatform.scoring.domain.model.aggregates.CreditScore;
import com.smartfinance.smartfinancedriveplatform.scoring.domain.model.valueobjects.ScoreId;

import java.util.List;
import java.util.Optional;

/**
 * Domain Repository interface for CreditScore aggregate root.
 */
public interface CreditScoreRepository {
    CreditScore save(CreditScore creditScore);
    Optional<CreditScore> findById(ScoreId scoreId);
    List<CreditScore> findByProfileId(String profileId);
    List<CreditScore> findAll();
    void deleteById(ScoreId scoreId);
    boolean existsById(ScoreId scoreId);
}
