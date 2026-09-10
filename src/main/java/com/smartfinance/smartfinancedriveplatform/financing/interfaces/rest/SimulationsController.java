package com.smartfinance.smartfinancedriveplatform.financing.interfaces.rest;

import com.smartfinance.smartfinancedriveplatform.financing.application.commandservices.SimulationCommandService;
import com.smartfinance.smartfinancedriveplatform.financing.application.queryservices.SimulationQueryService;
import com.smartfinance.smartfinancedriveplatform.financing.domain.model.commands.DeleteSimulationCommand;
import com.smartfinance.smartfinancedriveplatform.financing.domain.model.queries.GetAllSimulationsQuery;
import com.smartfinance.smartfinancedriveplatform.financing.domain.model.queries.GetSimulationByIdQuery;
import com.smartfinance.smartfinancedriveplatform.financing.domain.model.valueobjects.SimulationId;
import com.smartfinance.smartfinancedriveplatform.financing.interfaces.rest.resources.CreateSimulationResource;
import com.smartfinance.smartfinancedriveplatform.financing.interfaces.rest.resources.SimulationResource;
import com.smartfinance.smartfinancedriveplatform.financing.interfaces.rest.transform.CreateSimulationCommandFromResourceAssembler;
import com.smartfinance.smartfinancedriveplatform.financing.interfaces.rest.transform.SimulationResourceFromEntityAssembler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST controller for managing credit simulations and payment schedules.
 */
@RestController
@RequestMapping(value = "/api/v1/simulations", produces = "application/json")
public class SimulationsController {

    private final SimulationCommandService simulationCommandService;
    private final SimulationQueryService simulationQueryService;

    public SimulationsController(SimulationCommandService simulationCommandService,
                                 SimulationQueryService simulationQueryService) {
        this.simulationCommandService = simulationCommandService;
        this.simulationQueryService = simulationQueryService;
    }

    /**
     * POST /api/v1/simulations
     * Creates and computes a new credit simulation plan.
     */
    @PostMapping
    public ResponseEntity<SimulationResource> createSimulation(@RequestBody CreateSimulationResource resource) {
        var command = CreateSimulationCommandFromResourceAssembler.toCommandFromResource(resource);
        var simulationOpt = simulationCommandService.handle(command);
        return simulationOpt
                .map(simulation -> new ResponseEntity<>(
                        SimulationResourceFromEntityAssembler.toResourceFromEntity(simulation),
                        HttpStatus.CREATED
                ))
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    /**
     * GET /api/v1/simulations
     * Retrieves all credit simulations.
     */
    @GetMapping
    public ResponseEntity<List<SimulationResource>> getAllSimulations() {
        var query = new GetAllSimulationsQuery();
        var simulations = simulationQueryService.handle(query);
        var resources = simulations.stream()
                .map(SimulationResourceFromEntityAssembler::toResourceFromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(resources);
    }

    /**
     * GET /api/v1/simulations/{id}
     * Retrieves a credit simulation by ID with its full payment schedule and financial metrics.
     */
    @GetMapping("/{id}")
    public ResponseEntity<SimulationResource> getSimulationById(@PathVariable UUID id) {
        var query = new GetSimulationByIdQuery(new SimulationId(id));
        var simulationOpt = simulationQueryService.handle(query);
        return simulationOpt
                .map(simulation -> ResponseEntity.ok(SimulationResourceFromEntityAssembler.toResourceFromEntity(simulation)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * DELETE /api/v1/simulations/{id}
     * Deletes a credit simulation.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSimulation(@PathVariable UUID id) {
        var command = new DeleteSimulationCommand(new SimulationId(id));
        simulationCommandService.handle(command);
        return ResponseEntity.noContent().build();
    }
}
