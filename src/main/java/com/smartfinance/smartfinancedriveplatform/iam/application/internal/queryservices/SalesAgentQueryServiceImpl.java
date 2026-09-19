package com.smartfinance.smartfinancedriveplatform.iam.application.internal.queryservices;

import com.smartfinance.smartfinancedriveplatform.iam.application.queryservices.SalesAgentQueryService;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.aggregates.SalesAgent;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.queries.GetSalesAgentByIdQuery;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.queries.GetSalesAgentsForDealerQuery;
import com.smartfinance.smartfinancedriveplatform.iam.domain.repositories.SalesAgentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class SalesAgentQueryServiceImpl implements SalesAgentQueryService {

    private final SalesAgentRepository salesAgentRepository;

    public SalesAgentQueryServiceImpl(SalesAgentRepository salesAgentRepository) {
        this.salesAgentRepository = salesAgentRepository;
    }

    @Override
    public List<SalesAgent> handle(GetSalesAgentsForDealerQuery query) {
        return salesAgentRepository.findByDealerUserId(query.dealerUserId());
    }

    @Override
    public Optional<SalesAgent> handle(GetSalesAgentByIdQuery query) {
        return salesAgentRepository.findById(query.salesAgentId());
    }
}
