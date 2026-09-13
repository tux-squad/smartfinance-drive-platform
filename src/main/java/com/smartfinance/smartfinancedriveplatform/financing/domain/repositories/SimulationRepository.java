package com.smartfinance.smartfinancedriveplatform.financing.domain.repositories;

import com.smartfinance.smartfinancedriveplatform.financing.domain.model.aggregates.Simulation;
import com.smartfinance.smartfinancedriveplatform.financing.domain.model.valueobjects.SimulationId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Domain Repository interface for Simulation aggregate root.
 */
public interface SimulationRepository {
    Simulation save(Simulation simulation);
    Optional<Simulation> findById(SimulationId simulationId);
    List<Simulation> findAll();
    Page<Simulation> findAll(Pageable pageable);
    Page<Simulation> findByUserId(String userId, Pageable pageable);
    void deleteById(SimulationId simulationId);
    boolean existsById(SimulationId simulationId);
}
