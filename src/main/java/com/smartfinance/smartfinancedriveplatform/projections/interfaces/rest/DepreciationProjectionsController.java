package com.smartfinance.smartfinancedriveplatform.projections.interfaces.rest;

import com.smartfinance.smartfinancedriveplatform.projections.application.commandservices.DepreciationProjectionCommandService;
import com.smartfinance.smartfinancedriveplatform.projections.application.queryservices.DepreciationProjectionQueryService;
import com.smartfinance.smartfinancedriveplatform.projections.domain.model.commands.DeleteDepreciationProjectionCommand;
import com.smartfinance.smartfinancedriveplatform.projections.domain.model.queries.GetAllDepreciationProjectionsQuery;
import com.smartfinance.smartfinancedriveplatform.projections.domain.model.queries.GetDepreciationProjectionByIdQuery;
import com.smartfinance.smartfinancedriveplatform.projections.domain.model.queries.GetDepreciationProjectionsByVehicleIdQuery;
import com.smartfinance.smartfinancedriveplatform.projections.domain.model.valueobjects.ProjectionId;
import com.smartfinance.smartfinancedriveplatform.projections.interfaces.rest.resources.CalculateDepreciationProjectionResource;
import com.smartfinance.smartfinancedriveplatform.projections.interfaces.rest.resources.DepreciationProjectionResource;
import com.smartfinance.smartfinancedriveplatform.projections.interfaces.rest.transform.CalculateDepreciationProjectionCommandFromResourceAssembler;
import com.smartfinance.smartfinancedriveplatform.projections.interfaces.rest.transform.DepreciationProjectionResourceFromEntityAssembler;
import com.smartfinance.smartfinancedriveplatform.shared.infrastructure.security.OwnershipChecker;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST controller for managing vehicle depreciation projections and market trade-in advisory.
 */
@RestController
@RequestMapping(value = "/api/v1/depreciation-projections", produces = "application/json")
public class DepreciationProjectionsController {

    private final DepreciationProjectionCommandService commandService;
    private final DepreciationProjectionQueryService queryService;
    private final OwnershipChecker ownershipChecker;

    public DepreciationProjectionsController(DepreciationProjectionCommandService commandService,
                                             DepreciationProjectionQueryService queryService,
                                             OwnershipChecker ownershipChecker) {
        this.commandService = commandService;
        this.queryService = queryService;
        this.ownershipChecker = ownershipChecker;
    }

    /**
     * POST /api/v1/depreciation-projections/calculate
     * Calculates and saves a new vehicle depreciation projection.
     */
    @PostMapping("/calculate")
    public ResponseEntity<DepreciationProjectionResource> calculateProjection(@jakarta.validation.Valid @RequestBody CalculateDepreciationProjectionResource resource) {
        var command = CalculateDepreciationProjectionCommandFromResourceAssembler.toCommandFromResource(resource);
        var projectionOpt = commandService.handle(command);
        return projectionOpt
                .map(projection -> new ResponseEntity<>(
                        DepreciationProjectionResourceFromEntityAssembler.toResourceFromEntity(projection),
                        HttpStatus.CREATED
                ))
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    /**
     * GET /api/v1/depreciation-projections
     * Retrieves vehicle depreciation projections belonging to caller's vehicle(s) or all if ADMIN.
     */
    @GetMapping
    public ResponseEntity<org.springframework.data.domain.Page<DepreciationProjectionResource>> getAllProjections(
            @org.springframework.data.web.PageableDefault(size = 10) org.springframework.data.domain.Pageable pageable) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        var query = new GetAllDepreciationProjectionsQuery();
        var projectionsPage = queryService.handle(query, pageable);

        var resourcesPage = projectionsPage
                .filter(p -> ownershipChecker.isDepreciationProjectionOwner(p.getId().value(), auth))
                .map(DepreciationProjectionResourceFromEntityAssembler::toResourceFromEntity);

        return ResponseEntity.ok(resourcesPage);
    }

    /**
     * GET /api/v1/depreciation-projections/{id}
     * Retrieves a depreciation projection by ID if owned by caller or ADMIN.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @ownershipChecker.isDepreciationProjectionOwner(#id, authentication)")
    public ResponseEntity<DepreciationProjectionResource> getProjectionById(@PathVariable UUID id) {
        var query = new GetDepreciationProjectionByIdQuery(new ProjectionId(id));
        var projectionOpt = queryService.handle(query);
        return projectionOpt
                .map(projection -> ResponseEntity.ok(DepreciationProjectionResourceFromEntityAssembler.toResourceFromEntity(projection)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * GET /api/v1/depreciation-projections/vehicle/{vehicleId}
     * Retrieves depreciation projections for a specific vehicle if owned by caller or ADMIN.
     */
    @GetMapping("/vehicle/{vehicleId}")
    @PreAuthorize("hasRole('ADMIN') or @ownershipChecker.isVehicleOwnerStr(#vehicleId, authentication)")
    public ResponseEntity<List<DepreciationProjectionResource>> getProjectionsByVehicleId(@PathVariable String vehicleId) {
        var query = new GetDepreciationProjectionsByVehicleIdQuery(vehicleId);
        var projections = queryService.handle(query);
        var resources = projections.stream()
                .map(DepreciationProjectionResourceFromEntityAssembler::toResourceFromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(resources);
    }

    /**
     * DELETE /api/v1/depreciation-projections/{id}
     * Deletes a depreciation projection if owned by caller or ADMIN.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @ownershipChecker.isDepreciationProjectionOwner(#id, authentication)")
    public ResponseEntity<?> deleteProjection(@PathVariable UUID id) {
        var command = new DeleteDepreciationProjectionCommand(new ProjectionId(id));
        commandService.handle(command);
        return ResponseEntity.noContent().build();
    }
}
