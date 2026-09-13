package com.smartfinance.smartfinancedriveplatform.financing.application.internal.queryservices;

import com.smartfinance.smartfinancedriveplatform.financing.application.queryservices.SimulationQueryService;
import com.smartfinance.smartfinancedriveplatform.financing.domain.model.aggregates.Simulation;
import com.smartfinance.smartfinancedriveplatform.financing.domain.model.queries.GetAllSimulationsQuery;
import com.smartfinance.smartfinancedriveplatform.financing.domain.model.queries.GetSimulationByIdQuery;
import com.smartfinance.smartfinancedriveplatform.financing.domain.repositories.SimulationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of SimulationQueryService application service.
 */
@Service
public class SimulationQueryServiceImpl implements SimulationQueryService {

    private final SimulationRepository simulationRepository;

    public SimulationQueryServiceImpl(SimulationRepository simulationRepository) {
        this.simulationRepository = simulationRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Simulation> handle(GetSimulationByIdQuery query) {
        return simulationRepository.findById(query.simulationId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Simulation> handle(GetAllSimulationsQuery query) {
        return simulationRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Simulation> handle(GetAllSimulationsQuery query, Pageable pageable) {
        return simulationRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Simulation> handleGetByUserId(String userId, Pageable pageable) {
        return simulationRepository.findByUserId(userId, pageable);
    }
}
