package com.smartfinance.smartfinancedriveplatform.financing.application.internal.queryservices;

import com.smartfinance.smartfinancedriveplatform.financing.application.queryservices.CreditApplicationQueryService;
import com.smartfinance.smartfinancedriveplatform.financing.domain.model.aggregates.CreditApplication;
import com.smartfinance.smartfinancedriveplatform.financing.domain.model.queries.GetCreditApplicationByIdQuery;
import com.smartfinance.smartfinancedriveplatform.financing.domain.model.queries.GetCreditApplicationsByApplicantQuery;
import com.smartfinance.smartfinancedriveplatform.financing.domain.repositories.CreditApplicationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of CreditApplicationQueryService.
 */
@Service
public class CreditApplicationQueryServiceImpl implements CreditApplicationQueryService {

    private final CreditApplicationRepository repository;

    public CreditApplicationQueryServiceImpl(CreditApplicationRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CreditApplication> handle(GetCreditApplicationByIdQuery query) {
        return repository.findById(query.creditApplicationId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CreditApplication> handle(GetCreditApplicationsByApplicantQuery query) {
        return repository.findAllByApplicantUserId(query.applicantUserId());
    }
}
