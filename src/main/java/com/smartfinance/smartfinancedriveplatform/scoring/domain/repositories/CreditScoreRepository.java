package com.smartfinance.smartfinancedriveplatform.scoring.domain.repositories;

import com.smartfinance.smartfinancedriveplatform.scoring.domain.model.aggregates.CreditScore;
import com.smartfinance.smartfinancedriveplatform.scoring.domain.model.valueobjects.ScoreId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Domain Repository interface for CreditScore aggregate root.
 */
public interface CreditScoreRepository {
    CreditScore save(CreditScore creditScore);
    Optional<CreditScore> findById(ScoreId scoreId);
    List<CreditScore> findByProfileId(String profileId);
    Page<CreditScore> findByProfileId(String profileId, Pageable pageable);
    List<CreditScore> findAll();
    Page<CreditScore> findAll(Pageable pageable);
    void deleteById(ScoreId scoreId);
    boolean existsById(ScoreId scoreId);
}
