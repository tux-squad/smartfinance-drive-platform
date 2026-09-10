package com.smartfinance.smartfinancedriveplatform.partners.application.internal.queryservices;

import com.smartfinance.smartfinancedriveplatform.partners.application.queryservices.FinancialEntityQueryService;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.aggregates.FinancialEntity;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.queries.GetAllFinancialEntitiesQuery;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.queries.GetFinancialEntityByIdQuery;
import com.smartfinance.smartfinancedriveplatform.partners.domain.repositories.FinancialEntityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of FinancialEntityQueryService application service.
 * Handles read-only query operations for financial entities.
 */
@Service
public class FinancialEntityQueryServiceImpl implements FinancialEntityQueryService {

    private final FinancialEntityRepository financialEntityRepository;

    public FinancialEntityQueryServiceImpl(FinancialEntityRepository financialEntityRepository) {
        this.financialEntityRepository = financialEntityRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<FinancialEntity> handle(GetFinancialEntityByIdQuery query) {
        return financialEntityRepository.findById(query.financialEntityId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FinancialEntity> handle(GetAllFinancialEntitiesQuery query) {
        return financialEntityRepository.findAll();
    }
}
