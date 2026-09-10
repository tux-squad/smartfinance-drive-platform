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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    public DepreciationProjectionsController(DepreciationProjectionCommandService commandService,
                                             DepreciationProjectionQueryService queryService) {
        this.commandService = commandService;
        this.queryService = queryService;
    }

    /**
     * POST /api/v1/depreciation-projections/calculate
     * Calculates and saves a new vehicle depreciation projection.
     */
    @PostMapping("/calculate")
    public ResponseEntity<DepreciationProjectionResource> calculateProjection(@RequestBody CalculateDepreciationProjectionResource resource) {
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
     * Retrieves all vehicle depreciation projections.
     */
    @GetMapping
    public ResponseEntity<List<DepreciationProjectionResource>> getAllProjections() {
        var query = new GetAllDepreciationProjectionsQuery();
        var projections = queryService.handle(query);
        var resources = projections.stream()
                .map(DepreciationProjectionResourceFromEntityAssembler::toResourceFromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(resources);
    }

    /**
     * GET /api/v1/depreciation-projections/{id}
     * Retrieves a depreciation projection by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<DepreciationProjectionResource> getProjectionById(@PathVariable UUID id) {
        var query = new GetDepreciationProjectionByIdQuery(new ProjectionId(id));
        var projectionOpt = queryService.handle(query);
        return projectionOpt
                .map(projection -> ResponseEntity.ok(DepreciationProjectionResourceFromEntityAssembler.toResourceFromEntity(projection)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * GET /api/v1/depreciation-projections/vehicle/{vehicleId}
     * Retrieves depreciation projections for a specific vehicle.
     */
    @GetMapping("/vehicle/{vehicleId}")
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
     * Deletes a depreciation projection.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProjection(@PathVariable UUID id) {
        var command = new DeleteDepreciationProjectionCommand(new ProjectionId(id));
        commandService.handle(command);
        return ResponseEntity.noContent().build();
    }
}
