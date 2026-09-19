package com.smartfinance.smartfinancedriveplatform.crm.application.internal.queryservices;

import com.smartfinance.smartfinancedriveplatform.crm.application.queryservices.CrmQueryService;
import com.smartfinance.smartfinancedriveplatform.crm.domain.model.aggregates.Prospect;
import com.smartfinance.smartfinancedriveplatform.crm.domain.model.queries.GetProspectByIdQuery;
import com.smartfinance.smartfinancedriveplatform.crm.domain.model.queries.GetProspectsForDealerQuery;
import com.smartfinance.smartfinancedriveplatform.crm.domain.repositories.ProspectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CrmQueryServiceImpl implements CrmQueryService {

    private final ProspectRepository prospectRepository;

    public CrmQueryServiceImpl(ProspectRepository prospectRepository) {
        this.prospectRepository = prospectRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Prospect> handle(GetProspectsForDealerQuery query) {
        return prospectRepository.findAllByDealerUserId(query.dealerUserId());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Prospect> handle(GetProspectByIdQuery query) {
        return prospectRepository.findById(query.prospectId());
    }
}
