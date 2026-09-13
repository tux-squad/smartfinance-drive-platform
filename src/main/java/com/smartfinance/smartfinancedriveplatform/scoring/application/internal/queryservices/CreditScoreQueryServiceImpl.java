package com.smartfinance.smartfinancedriveplatform.scoring.application.internal.queryservices;

import com.smartfinance.smartfinancedriveplatform.scoring.application.queryservices.CreditScoreQueryService;
import com.smartfinance.smartfinancedriveplatform.scoring.domain.model.aggregates.CreditScore;
import com.smartfinance.smartfinancedriveplatform.scoring.domain.model.queries.GetAllCreditScoresQuery;
import com.smartfinance.smartfinancedriveplatform.scoring.domain.model.queries.GetCreditScoreByIdQuery;
import com.smartfinance.smartfinancedriveplatform.scoring.domain.model.queries.GetCreditScoreByProfileIdQuery;
import com.smartfinance.smartfinancedriveplatform.scoring.domain.repositories.CreditScoreRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of CreditScoreQueryService application service.
 */
@Service
public class CreditScoreQueryServiceImpl implements CreditScoreQueryService {

    private final CreditScoreRepository creditScoreRepository;

    public CreditScoreQueryServiceImpl(CreditScoreRepository creditScoreRepository) {
        this.creditScoreRepository = creditScoreRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CreditScore> handle(GetCreditScoreByIdQuery query) {
        return creditScoreRepository.findById(query.scoreId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CreditScore> handle(GetCreditScoreByProfileIdQuery query) {
        return creditScoreRepository.findByProfileId(query.profileId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CreditScore> handle(GetAllCreditScoresQuery query) {
        return creditScoreRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CreditScore> handle(GetAllCreditScoresQuery query, Pageable pageable) {
        return creditScoreRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CreditScore> handle(GetCreditScoreByProfileIdQuery query, Pageable pageable) {
        return creditScoreRepository.findByProfileId(query.profileId(), pageable);
    }
}
