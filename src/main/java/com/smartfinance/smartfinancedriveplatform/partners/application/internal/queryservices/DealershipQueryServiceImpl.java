package com.smartfinance.smartfinancedriveplatform.partners.application.internal.queryservices;

import com.smartfinance.smartfinancedriveplatform.partners.application.queryservices.DealershipQueryService;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.aggregates.Dealership;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.queries.GetAllDealershipsQuery;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.queries.GetDealershipByIdQuery;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.queries.GetDealershipByUserIdQuery;
import com.smartfinance.smartfinancedriveplatform.partners.domain.repositories.DealershipRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Implementation of DealershipQueryService.
 */
@Service
public class DealershipQueryServiceImpl implements DealershipQueryService {

    private final DealershipRepository dealershipRepository;

    public DealershipQueryServiceImpl(DealershipRepository dealershipRepository) {
        this.dealershipRepository = dealershipRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Dealership> handle(GetDealershipByIdQuery query) {
        return dealershipRepository.findById(query.dealershipId());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Dealership> handle(GetDealershipByUserIdQuery query) {
        return dealershipRepository.findByUserId(query.userId());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Dealership> handle(GetAllDealershipsQuery query, Pageable pageable) {
        return dealershipRepository.findAll(query.search(), pageable);
    }
}
