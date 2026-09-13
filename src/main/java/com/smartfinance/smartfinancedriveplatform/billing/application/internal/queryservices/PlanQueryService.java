package com.smartfinance.smartfinancedriveplatform.billing.application.internal.queryservices;

import com.smartfinance.smartfinancedriveplatform.billing.domain.model.aggregates.Plan;
import com.smartfinance.smartfinancedriveplatform.billing.domain.model.queries.GetAllActivePlansQuery;
import com.smartfinance.smartfinancedriveplatform.billing.domain.model.queries.GetPlanByIdQuery;

import java.util.List;
import java.util.Optional;

/**
 * Application Query Service interface for reading SaaS plans.
 */
public interface PlanQueryService {
    List<Plan> handle(GetAllActivePlansQuery query);
    Optional<Plan> handle(GetPlanByIdQuery query);
}
