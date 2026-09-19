package com.smartfinance.smartfinancedriveplatform.crm.application.queryservices;

import com.smartfinance.smartfinancedriveplatform.crm.domain.model.aggregates.Prospect;
import com.smartfinance.smartfinancedriveplatform.crm.domain.model.queries.GetProspectByIdQuery;
import com.smartfinance.smartfinancedriveplatform.crm.domain.model.queries.GetProspectsForDealerQuery;

import java.util.List;
import java.util.Optional;

public interface CrmQueryService {

    List<Prospect> handle(GetProspectsForDealerQuery query);

    Optional<Prospect> handle(GetProspectByIdQuery query);
}
