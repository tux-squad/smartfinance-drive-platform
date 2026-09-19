package com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest;

import com.smartfinance.smartfinancedriveplatform.iam.application.commandservices.SalesAgentCommandService;
import com.smartfinance.smartfinancedriveplatform.iam.application.queryservices.SalesAgentQueryService;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.CreateSalesAgentCommand;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.ReassignLeadsCommand;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.UpdateSalesAgentCommand;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.queries.GetSalesAgentsForDealerQuery;
import com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.resources.CreateUpdateSalesAgentResource;
import com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.resources.ReassignLeadsResource;
import com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.resources.SalesAgentResource;
import com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.transform.SalesAgentResourceFromEntityAssembler;
import com.smartfinance.smartfinancedriveplatform.shared.infrastructure.security.SecurityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST Controller for Dealership Sales Agents management and lead reassignments.
 */
@RestController
@RequestMapping(value = "/api/v1/dealers/me/sales-agents", produces = "application/json")
public class SalesAgentsController {

    private final SalesAgentCommandService commandService;
    private final SalesAgentQueryService queryService;

    public SalesAgentsController(SalesAgentCommandService commandService, SalesAgentQueryService queryService) {
        this.commandService = commandService;
        this.queryService = queryService;
    }

    /**
     * GET /api/v1/dealers/me/sales-agents
     * Returns list of sales agents associated with the authenticated dealer.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('DEALER', 'ADMIN')")
    public ResponseEntity<List<SalesAgentResource>> getMySalesAgents() {
        String authDealerUserId = SecurityUtils.getRequiredCurrentUserId();
        var query = new GetSalesAgentsForDealerQuery(authDealerUserId);
        var agents = queryService.handle(query);
        var resources = agents.stream()
                .map(SalesAgentResourceFromEntityAssembler::toResourceFromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(resources);
    }

    /**
     * POST /api/v1/dealers/me/sales-agents
     * Creates a new sales agent for the authenticated dealer.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('DEALER', 'ADMIN')")
    public ResponseEntity<SalesAgentResource> createSalesAgent(@RequestBody CreateUpdateSalesAgentResource resource) {
        String authDealerUserId = SecurityUtils.getRequiredCurrentUserId();
        var command = new CreateSalesAgentCommand(
                authDealerUserId,
                resource.fullName(),
                resource.email(),
                resource.phone()
        );
        var agent = commandService.handle(command);
        return new ResponseEntity<>(SalesAgentResourceFromEntityAssembler.toResourceFromEntity(agent), HttpStatus.CREATED);
    }

    /**
     * PUT /api/v1/dealers/me/sales-agents/{id}
     * Updates an existing sales agent details.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('DEALER', 'ADMIN')")
    public ResponseEntity<SalesAgentResource> updateSalesAgent(
            @PathVariable UUID id,
            @RequestBody CreateUpdateSalesAgentResource resource) {
        String authDealerUserId = SecurityUtils.getRequiredCurrentUserId();
        boolean activeStatus = resource.active() == null || resource.active();
        var command = new UpdateSalesAgentCommand(
                id,
                authDealerUserId,
                resource.fullName(),
                resource.email(),
                resource.phone(),
                activeStatus
        );
        var updated = commandService.handle(command);
        return ResponseEntity.ok(SalesAgentResourceFromEntityAssembler.toResourceFromEntity(updated));
    }

    /**
     * POST /api/v1/dealers/me/sales-agents/{id}/reassign-leads
     * Reassigns all CRM prospect leads from target source agent ID to targetAgentId.
     */
    @PostMapping("/{id}/reassign-leads")
    @PreAuthorize("hasAnyRole('DEALER', 'ADMIN')")
    public ResponseEntity<Void> reassignLeads(
            @PathVariable UUID id,
            @RequestBody ReassignLeadsResource resource) {
        String authDealerUserId = SecurityUtils.getRequiredCurrentUserId();
        var command = new ReassignLeadsCommand(
                id,
                resource.targetAgentId(),
                authDealerUserId
        );
        commandService.handle(command);
        return ResponseEntity.noContent().build();
    }
}
