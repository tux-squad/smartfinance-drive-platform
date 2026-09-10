package com.smartfinance.smartfinancedriveplatform.financing.domain.repositories;

import com.smartfinance.smartfinancedriveplatform.financing.domain.model.aggregates.Simulation;
import com.smartfinance.smartfinancedriveplatform.financing.domain.model.valueobjects.SimulationId;

import java.util.List;
import java.util.Optional;

/**
 * Domain Repository interface for Simulation aggregate root.
 */
public interface SimulationRepository {
    Simulation save(Simulation simulation);
    Optional<Simulation> findById(SimulationId simulationId);
    List<Simulation> findAll();
    void deleteById(SimulationId simulationId);
    boolean existsById(SimulationId simulationId);
}
