package com.smartfinance.smartfinancedriveplatform.partners.interfaces.rest;

import com.smartfinance.smartfinancedriveplatform.partners.application.commandservices.FinancialEntityCommandService;
import com.smartfinance.smartfinancedriveplatform.partners.application.queryservices.FinancialEntityQueryService;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.commands.DeleteFinancialEntityCommand;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.queries.GetAllFinancialEntitiesQuery;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.queries.GetFinancialEntityByIdQuery;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.valueobjects.FinancialEntityId;
import com.smartfinance.smartfinancedriveplatform.partners.interfaces.rest.resources.AddRateBenchmarkResource;
import com.smartfinance.smartfinancedriveplatform.partners.interfaces.rest.resources.CreateFinancialEntityResource;
import com.smartfinance.smartfinancedriveplatform.partners.interfaces.rest.resources.FinancialEntityResource;
import com.smartfinance.smartfinancedriveplatform.partners.interfaces.rest.resources.UpdateFinancialEntityResource;
import com.smartfinance.smartfinancedriveplatform.partners.interfaces.rest.transform.AddRateBenchmarkCommandFromResourceAssembler;
import com.smartfinance.smartfinancedriveplatform.partners.interfaces.rest.transform.CreateFinancialEntityCommandFromResourceAssembler;
import com.smartfinance.smartfinancedriveplatform.partners.interfaces.rest.transform.FinancialEntityResourceFromEntityAssembler;
import com.smartfinance.smartfinancedriveplatform.partners.interfaces.rest.transform.UpdateFinancialEntityCommandFromResourceAssembler;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST controller for managing financial entities and their rate benchmarks.
 * Follows the CQRS pattern: delegates write commands to FinancialEntityCommandService
 * and read queries to FinancialEntityQueryService.
 */
@RestController
@RequestMapping(value = "/api/v1/financial-entities", produces = "application/json")
public class FinancialEntitiesController {

    private final FinancialEntityCommandService financialEntityCommandService;
    private final FinancialEntityQueryService financialEntityQueryService;

    public FinancialEntitiesController(FinancialEntityCommandService financialEntityCommandService, 
                                       FinancialEntityQueryService financialEntityQueryService) {
        this.financialEntityCommandService = financialEntityCommandService;
        this.financialEntityQueryService = financialEntityQueryService;
    }

    /**
     * POST /api/v1/financial-entities
     * Registers a new financial entity.
     *
     * @param resource The creation payload.
     * @return The created financial entity resource payload.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FinancialEntityResource> createFinancialEntity(@RequestBody CreateFinancialEntityResource resource) {
        var command = CreateFinancialEntityCommandFromResourceAssembler.toCommandFromResource(resource);
        var entityOpt = financialEntityCommandService.handle(command);
        return entityOpt
                .map(entity -> new ResponseEntity<>(
                        FinancialEntityResourceFromEntityAssembler.toResourceFromEntity(entity),
                        HttpStatus.CREATED
                ))
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    /**
     * GET /api/v1/financial-entities
     * Retrieves all registered financial entities.
     *
     * @return List of financial entity resources.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'FINANCIAL_ANALYST')")
    public ResponseEntity<List<FinancialEntityResource>> getAllFinancialEntities() {
        var query = new GetAllFinancialEntitiesQuery();
        var entities = financialEntityQueryService.handle(query);
        var resources = entities.stream()
                .map(FinancialEntityResourceFromEntityAssembler::toResourceFromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(resources);
    }

    /**
     * GET /api/v1/financial-entities/{id}
     * Retrieves a financial entity details by ID.
     *
     * @param id The financial entity UUID.
     * @return The financial entity resource payload.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'FINANCIAL_ANALYST')")
    public ResponseEntity<FinancialEntityResource> getFinancialEntityById(@PathVariable UUID id) {
        var query = new GetFinancialEntityByIdQuery(new FinancialEntityId(id));
        var entityOpt = financialEntityQueryService.handle(query);
        return entityOpt
                .map(entity -> ResponseEntity.ok(FinancialEntityResourceFromEntityAssembler.toResourceFromEntity(entity)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * POST /api/v1/financial-entities/{id}/rate-benchmarks
     * Adds a new rate benchmark to a financial entity.
     *
     * @param id       The financial entity UUID.
     * @param resource The rate benchmark payload.
     * @return The updated financial entity resource payload.
     */
    @PostMapping("/{id}/rate-benchmarks")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FinancialEntityResource> addRateBenchmark(
            @PathVariable UUID id,
            @RequestBody AddRateBenchmarkResource resource) {
        var command = AddRateBenchmarkCommandFromResourceAssembler.toCommandFromResource(id, resource);
        var entityOpt = financialEntityCommandService.handle(command);
        return entityOpt
                .map(entity -> new ResponseEntity<>(
                        FinancialEntityResourceFromEntityAssembler.toResourceFromEntity(entity),
                        HttpStatus.CREATED
                ))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * PUT /api/v1/financial-entities/{id}
     * Updates an existing financial entity name.
     *
     * @param id       The financial entity UUID.
     * @param resource The update payload.
     * @return The updated financial entity resource payload.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FinancialEntityResource> updateFinancialEntity(
            @PathVariable UUID id,
            @RequestBody UpdateFinancialEntityResource resource) {
        var command = UpdateFinancialEntityCommandFromResourceAssembler.toCommandFromResource(id, resource);
        var entityOpt = financialEntityCommandService.handle(command);
        return entityOpt
                .map(entity -> ResponseEntity.ok(FinancialEntityResourceFromEntityAssembler.toResourceFromEntity(entity)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * DELETE /api/v1/financial-entities/{id}
     * Deletes a financial entity.
     *
     * @param id The financial entity UUID.
     * @return 204 No Content.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteFinancialEntity(@PathVariable UUID id) {
        var command = new DeleteFinancialEntityCommand(new FinancialEntityId(id));
        financialEntityCommandService.handle(command);
        return ResponseEntity.noContent().build();
    }
}
