package com.smartfinance.smartfinancedriveplatform.financing.application.queryservices;

import com.smartfinance.smartfinancedriveplatform.financing.domain.model.aggregates.Simulation;
import com.smartfinance.smartfinancedriveplatform.financing.domain.model.queries.GetAllSimulationsQuery;
import com.smartfinance.smartfinancedriveplatform.financing.domain.model.queries.GetSimulationByIdQuery;

import java.util.List;
import java.util.Optional;

/**
 * Query Service interface for retrieving Credit Simulations.
 */
public interface SimulationQueryService {
    Optional<Simulation> handle(GetSimulationByIdQuery query);
    List<Simulation> handle(GetAllSimulationsQuery query);
}
