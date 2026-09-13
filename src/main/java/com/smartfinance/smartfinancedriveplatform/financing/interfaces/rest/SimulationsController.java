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
import com.smartfinance.smartfinancedriveplatform.shared.infrastructure.security.SecurityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
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
     * Creates and computes a new credit simulation plan strictly associated with the authenticated user.
     */
    @PostMapping
    public ResponseEntity<SimulationResource> createSimulation(@jakarta.validation.Valid @RequestBody CreateSimulationResource resource) {
        String authUserId = SecurityUtils.getRequiredCurrentUserId();
        var command = CreateSimulationCommandFromResourceAssembler.toCommandFromResource(resource, authUserId);
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
     * Retrieves credit simulations belonging to the authenticated user (or all if ADMIN).
     */
    @GetMapping
    public ResponseEntity<org.springframework.data.domain.Page<SimulationResource>> getAllSimulations(
            @org.springframework.data.web.PageableDefault(size = 10) org.springframework.data.domain.Pageable pageable) {
        String authUserId = SecurityUtils.getCurrentUserId().orElse(null);

        org.springframework.data.domain.Page<Simulation> simulationsPage = (authUserId != null)
                ? simulationQueryService.handleGetByUserId(authUserId, pageable)
                : simulationQueryService.handle(new GetAllSimulationsQuery(), pageable);

        var resourcesPage = simulationsPage.map(SimulationResourceFromEntityAssembler::toResourceFromEntity);
        return ResponseEntity.ok(resourcesPage);
    }

    /**
     * GET /api/v1/simulations/{id}
     * Retrieves a credit simulation by ID if owned by caller or ADMIN.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @ownershipChecker.isSimulationOwner(#id, authentication)")
    public ResponseEntity<SimulationResource> getSimulationById(@PathVariable UUID id) {
        var query = new GetSimulationByIdQuery(new SimulationId(id));
        var simulationOpt = simulationQueryService.handle(query);
        return simulationOpt
                .map(simulation -> ResponseEntity.ok(SimulationResourceFromEntityAssembler.toResourceFromEntity(simulation)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * DELETE /api/v1/simulations/{id}
     * Deletes a credit simulation if owned by authenticated user.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("@ownershipChecker.isSimulationOwner(#id, authentication)")
    public ResponseEntity<?> deleteSimulation(@PathVariable UUID id) {
        var command = new DeleteSimulationCommand(new SimulationId(id));
        simulationCommandService.handle(command);
        return ResponseEntity.noContent().build();
    }
}
