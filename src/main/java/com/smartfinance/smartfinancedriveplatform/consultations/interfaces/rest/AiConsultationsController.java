package com.smartfinance.smartfinancedriveplatform.consultations.interfaces.rest;

import com.smartfinance.smartfinancedriveplatform.catalog.application.queryservices.VehicleQueryService;
import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.queries.GetAllVehiclesQuery;
import com.smartfinance.smartfinancedriveplatform.catalog.interfaces.rest.resources.VehicleResource;
import com.smartfinance.smartfinancedriveplatform.catalog.interfaces.rest.transform.VehicleResourceFromEntityAssembler;
import com.smartfinance.smartfinancedriveplatform.consultations.application.commandservices.AiConsultationCommandService;
import com.smartfinance.smartfinancedriveplatform.consultations.application.queryservices.AiConsultationQueryService;
import com.smartfinance.smartfinancedriveplatform.consultations.domain.model.commands.CreateAiConsultationCommand;
import com.smartfinance.smartfinancedriveplatform.consultations.domain.model.queries.GetAiConsultationHistoryForUserQuery;
import com.smartfinance.smartfinancedriveplatform.consultations.interfaces.rest.resources.AiConsultationResource;
import com.smartfinance.smartfinancedriveplatform.consultations.interfaces.rest.resources.CreateAiConsultationResource;
import com.smartfinance.smartfinancedriveplatform.consultations.interfaces.rest.transform.AiConsultationResourceFromEntityAssembler;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.valueobjects.Money;
import com.smartfinance.smartfinancedriveplatform.shared.infrastructure.security.SecurityUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for AI Financial Advisor consultations.
 * Supports dual path mappings: /api/v1/consultations and /api/v1/ai/consultations.
 */
@RestController
@RequestMapping(value = {"/api/v1/consultations", "/api/v1/ai/consultations"}, produces = "application/json")
public class AiConsultationsController {

    private final AiConsultationCommandService commandService;
    private final AiConsultationQueryService queryService;
    private final VehicleQueryService vehicleQueryService;

    public AiConsultationsController(AiConsultationCommandService commandService,
                                     AiConsultationQueryService queryService,
                                     VehicleQueryService vehicleQueryService) {
        this.commandService = commandService;
        this.queryService = queryService;
        this.vehicleQueryService = vehicleQueryService;
    }

    /**
     * POST /api/v1/consultations/chat or /api/v1/consultations
     * Submits a financial consultation prompt to the AI advisor engine.
     */
    @PostMapping(value = {"", "/chat"})
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AiConsultationResource> createConsultation(
            @jakarta.validation.Valid @RequestBody CreateAiConsultationResource resource) {
        String authUserId = SecurityUtils.getRequiredCurrentUserId();
        String currency = resource.currency() != null ? resource.currency() : "PEN";
        Money income = resource.monthlyIncome() != null ? new Money(resource.monthlyIncome(), currency) : null;
        Money budget = resource.maxBudget() != null ? new Money(resource.maxBudget(), currency) : null;

        var command = new CreateAiConsultationCommand(authUserId, resource.prompt(), income, budget);
        var consultationOpt = commandService.handle(command);
        return consultationOpt
                .map(c -> new ResponseEntity<>(AiConsultationResourceFromEntityAssembler.toResourceFromEntity(c), HttpStatus.CREATED))
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    /**
     * GET /api/v1/consultations/history
     * Retrieves historical AI advisor consultations for the authenticated user.
     */
    @GetMapping("/history")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<AiConsultationResource>> getConsultationHistory() {
        String authUserId = SecurityUtils.getRequiredCurrentUserId();
        var query = new GetAiConsultationHistoryForUserQuery(authUserId);
        var history = queryService.handle(query);
        var resources = history.stream()
                .map(AiConsultationResourceFromEntityAssembler::toResourceFromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(resources);
    }

    /**
     * GET /api/v1/consultations/recommendations
     * Retrieves AI-curated vehicle recommendations matching user profile/financial capacity.
     */
    @GetMapping("/recommendations")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<VehicleResource>> getAiVehicleRecommendations() {
        var page = vehicleQueryService.handle(new GetAllVehiclesQuery(), PageRequest.of(0, 6));
        var resources = page.getContent().stream()
                .map(VehicleResourceFromEntityAssembler::toResourceFromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(resources);
    }
}
