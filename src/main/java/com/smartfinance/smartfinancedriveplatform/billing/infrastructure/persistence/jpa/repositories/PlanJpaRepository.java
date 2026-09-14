package com.smartfinance.smartfinancedriveplatform.billing.infrastructure.persistence.jpa.repositories;

import com.smartfinance.smartfinancedriveplatform.billing.domain.model.aggregates.Plan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlanJpaRepository extends JpaRepository<Plan, Long> {
    Optional<Plan> findByName(String name);
    List<Plan> findByActiveTrue();
}
