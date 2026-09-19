package com.smartfinance.smartfinancedriveplatform.iam.application.queryservices;

import com.smartfinance.smartfinancedriveplatform.iam.domain.model.aggregates.SalesAgent;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.queries.GetSalesAgentByIdQuery;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.queries.GetSalesAgentsForDealerQuery;

import java.util.List;
import java.util.Optional;

public interface SalesAgentQueryService {
    List<SalesAgent> handle(GetSalesAgentsForDealerQuery query);
    Optional<SalesAgent> handle(GetSalesAgentByIdQuery query);
}
