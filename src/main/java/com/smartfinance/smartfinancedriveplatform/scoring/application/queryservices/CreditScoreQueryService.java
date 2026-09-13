package com.smartfinance.smartfinancedriveplatform.scoring.application.queryservices;

import com.smartfinance.smartfinancedriveplatform.scoring.domain.model.aggregates.CreditScore;
import com.smartfinance.smartfinancedriveplatform.scoring.domain.model.queries.GetAllCreditScoresQuery;
import com.smartfinance.smartfinancedriveplatform.scoring.domain.model.queries.GetCreditScoreByIdQuery;
import com.smartfinance.smartfinancedriveplatform.scoring.domain.model.queries.GetCreditScoreByProfileIdQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Query Service interface for retrieving CreditScore evaluations.
 */
public interface CreditScoreQueryService {
    Optional<CreditScore> handle(GetCreditScoreByIdQuery query);
    List<CreditScore> handle(GetCreditScoreByProfileIdQuery query);
    List<CreditScore> handle(GetAllCreditScoresQuery query);
    Page<CreditScore> handle(GetAllCreditScoresQuery query, Pageable pageable);
    Page<CreditScore> handle(GetCreditScoreByProfileIdQuery query, Pageable pageable);
}
