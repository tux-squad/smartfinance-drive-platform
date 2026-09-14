package com.smartfinance.smartfinancedriveplatform.billing.domain.repositories;

import com.smartfinance.smartfinancedriveplatform.billing.domain.model.aggregates.Plan;

import java.util.List;
import java.util.Optional;

/**
 * Domain Repository interface for managing {@link Plan} aggregates.
 */
public interface PlanRepository {
    Plan save(Plan plan);
    Optional<Plan> findById(Long id);
    Optional<Plan> findByName(String name);
    List<Plan> findAllActive();
}
