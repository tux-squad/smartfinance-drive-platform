package com.smartfinance.smartfinancedriveplatform.billing.infrastructure.persistence.jpa.adapters;

import com.smartfinance.smartfinancedriveplatform.billing.domain.model.aggregates.Plan;
import com.smartfinance.smartfinancedriveplatform.billing.domain.repositories.PlanRepository;
import com.smartfinance.smartfinancedriveplatform.billing.infrastructure.persistence.jpa.repositories.PlanJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class PlanRepositoryAdapter implements PlanRepository {

    private final PlanJpaRepository planJpaRepository;

    public PlanRepositoryAdapter(PlanJpaRepository planJpaRepository) {
        this.planJpaRepository = planJpaRepository;
    }

    @Override
    public Plan save(Plan plan) {
        return planJpaRepository.save(plan);
    }

    @Override
    public Optional<Plan> findById(Long id) {
        return planJpaRepository.findById(id);
    }

    @Override
    public Optional<Plan> findByName(String name) {
        return planJpaRepository.findByName(name);
    }

    @Override
    public List<Plan> findAllActive() {
        return planJpaRepository.findByActiveTrue();
    }
}
