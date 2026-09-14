package com.smartfinance.smartfinancedriveplatform.billing.interfaces.rest;

import com.smartfinance.smartfinancedriveplatform.billing.application.internal.commandservices.PlanCommandService;
import com.smartfinance.smartfinancedriveplatform.billing.application.internal.queryservices.PlanQueryService;
import com.smartfinance.smartfinancedriveplatform.billing.domain.model.commands.CreatePlanCommand;
import com.smartfinance.smartfinancedriveplatform.billing.domain.model.queries.GetAllActivePlansQuery;
import com.smartfinance.smartfinancedriveplatform.billing.domain.model.queries.GetPlanByIdQuery;
import com.smartfinance.smartfinancedriveplatform.billing.interfaces.rest.resources.CreatePlanResource;
import com.smartfinance.smartfinancedriveplatform.billing.interfaces.rest.resources.PlanResource;
import com.smartfinance.smartfinancedriveplatform.billing.interfaces.rest.transform.PlanResourceFromEntityAssembler;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for SaaS Subscription Plan management.
 */
@RestController
@RequestMapping(value = "/api/v1/billing/plans", produces = MediaType.APPLICATION_JSON_VALUE)
public class PlansController {

    private final PlanCommandService planCommandService;
    private final PlanQueryService planQueryService;

    public PlansController(PlanCommandService planCommandService, PlanQueryService planQueryService) {
        this.planCommandService = planCommandService;
        this.planQueryService = planQueryService;
    }

    /**
     * Retrieves all active subscription plans. Available to all authenticated users.
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<PlanResource>> getAllActivePlans() {
        var plans = planQueryService.handle(new GetAllActivePlansQuery());
        var resources = plans.stream()
                .map(PlanResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    /**
     * Retrieves a specific plan by ID. Available to all authenticated users.
     */
    @GetMapping("/{planId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PlanResource> getPlanById(@PathVariable Long planId) {
        var planOpt = planQueryService.handle(new GetPlanByIdQuery(planId));
        return planOpt
                .map(plan -> ResponseEntity.ok(PlanResourceFromEntityAssembler.toResourceFromEntity(plan)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Creates a new subscription plan tier. Restricted to ADMIN role.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PlanResource> createPlan(@Valid @RequestBody CreatePlanResource resource) {
        var command = new CreatePlanCommand(
                resource.name(),
                resource.description(),
                resource.price(),
                resource.currency(),
                resource.billingCycle(),
                resource.maxVehicleListings(),
                resource.maxSimulationsPerMonth(),
                resource.stripePriceId()
        );
        var createdPlanOpt = planCommandService.handle(command);
        return createdPlanOpt
                .map(plan -> ResponseEntity.status(HttpStatus.CREATED).body(PlanResourceFromEntityAssembler.toResourceFromEntity(plan)))
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }
}
