package com.smartfinance.smartfinancedriveplatform.financing.interfaces.rest;

import com.smartfinance.smartfinancedriveplatform.financing.application.commandservices.CreditApplicationCommandService;
import com.smartfinance.smartfinancedriveplatform.financing.application.commandservices.SimulationCommandService;
import com.smartfinance.smartfinancedriveplatform.financing.application.queryservices.SimulationQueryService;
import com.smartfinance.smartfinancedriveplatform.financing.domain.model.aggregates.Simulation;
import com.smartfinance.smartfinancedriveplatform.financing.domain.model.commands.CreateCreditApplicationCommand;
import com.smartfinance.smartfinancedriveplatform.financing.domain.model.commands.DeleteSimulationCommand;
import com.smartfinance.smartfinancedriveplatform.financing.domain.model.queries.GetAllSimulationsQuery;
import com.smartfinance.smartfinancedriveplatform.financing.domain.model.queries.GetSimulationByIdQuery;
import com.smartfinance.smartfinancedriveplatform.financing.domain.model.valueobjects.SimulationId;
import com.smartfinance.smartfinancedriveplatform.financing.interfaces.rest.resources.CreateSimulationResource;
import com.smartfinance.smartfinancedriveplatform.financing.interfaces.rest.resources.CreditApplicationResource;
import com.smartfinance.smartfinancedriveplatform.financing.interfaces.rest.resources.SimulationResource;
import com.smartfinance.smartfinancedriveplatform.financing.interfaces.rest.transform.CreateSimulationCommandFromResourceAssembler;
import com.smartfinance.smartfinancedriveplatform.financing.interfaces.rest.transform.CreditApplicationResourceFromEntityAssembler;
import com.smartfinance.smartfinancedriveplatform.financing.interfaces.rest.transform.SimulationResourceFromEntityAssembler;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.valueobjects.Money;
import com.smartfinance.smartfinancedriveplatform.shared.infrastructure.security.SecurityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * REST controller for managing credit simulations and payment schedules.
 */
@RestController
@RequestMapping(value = "/api/v1/simulations", produces = "application/json")
public class SimulationsController {

    private final SimulationCommandService simulationCommandService;
    private final SimulationQueryService simulationQueryService;
    private final CreditApplicationCommandService creditApplicationCommandService;

    public SimulationsController(SimulationCommandService simulationCommandService,
                                 SimulationQueryService simulationQueryService,
                                 CreditApplicationCommandService creditApplicationCommandService) {
        this.simulationCommandService = simulationCommandService;
        this.simulationQueryService = simulationQueryService;
        this.creditApplicationCommandService = creditApplicationCommandService;
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
     * POST /api/v1/simulations/{id}/apply
     * Promotes a saved simulation plan directly into a formal bank credit application.
     */
    @PostMapping("/{id}/apply")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CreditApplicationResource> applyFromSimulation(@PathVariable UUID id) {
        String authUserId = SecurityUtils.getRequiredCurrentUserId();
        var simulationOpt = simulationQueryService.handle(new GetSimulationByIdQuery(new SimulationId(id)));
        if (simulationOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var sim = simulationOpt.get();
        UUID vehicleUuid = UUID.fromString(sim.getVehicleId());
        UUID entityUuid = UUID.fromString(sim.getFinancialEntityId());

        Money monthlyIncome = new Money(new BigDecimal("3000.00"), sim.getFinancedAmount().currency());
        Money downPayment = sim.getDownPaymentAmount();

        var command = new CreateCreditApplicationCommand(
                authUserId,
                vehicleUuid,
                entityUuid,
                sim.getId().value(),
                sim.getFinancedAmount(),
                downPayment,
                sim.getLoanTermMonths(),
                monthlyIncome,
                "EMPLOYED"
        );

        var appOpt = creditApplicationCommandService.handle(command);
        return appOpt
                .map(app -> new ResponseEntity<>(
                        CreditApplicationResourceFromEntityAssembler.toResourceFromEntity(app),
                        HttpStatus.CREATED
                ))
                .orElseGet(() -> ResponseEntity.badRequest().build());
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
