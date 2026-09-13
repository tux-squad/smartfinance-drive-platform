package com.smartfinance.smartfinancedriveplatform.billing.application.internal.queryservices;

import com.smartfinance.smartfinancedriveplatform.billing.domain.model.aggregates.Plan;
import com.smartfinance.smartfinancedriveplatform.billing.domain.model.queries.GetAllActivePlansQuery;
import com.smartfinance.smartfinancedriveplatform.billing.domain.model.queries.GetPlanByIdQuery;
import com.smartfinance.smartfinancedriveplatform.billing.domain.repositories.PlanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PlanQueryServiceImpl implements PlanQueryService {

    private final PlanRepository planRepository;

    public PlanQueryServiceImpl(PlanRepository planRepository) {
        this.planRepository = planRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Plan> handle(GetAllActivePlansQuery query) {
        return planRepository.findAllActive();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Plan> handle(GetPlanByIdQuery query) {
        return planRepository.findById(query.planId());
    }
}
