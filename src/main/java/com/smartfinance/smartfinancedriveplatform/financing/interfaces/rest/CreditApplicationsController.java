package com.smartfinance.smartfinancedriveplatform.financing.interfaces.rest;

import com.smartfinance.smartfinancedriveplatform.financing.application.commandservices.CreditApplicationCommandService;
import com.smartfinance.smartfinancedriveplatform.financing.application.queryservices.CreditApplicationQueryService;
import com.smartfinance.smartfinancedriveplatform.financing.domain.model.commands.CreateCreditApplicationCommand;
import com.smartfinance.smartfinancedriveplatform.financing.domain.model.commands.UpdateCreditApplicationStatusCommand;
import com.smartfinance.smartfinancedriveplatform.financing.domain.model.queries.GetCreditApplicationByIdQuery;
import com.smartfinance.smartfinancedriveplatform.financing.domain.model.queries.GetCreditApplicationsByApplicantQuery;
import com.smartfinance.smartfinancedriveplatform.financing.domain.model.valueobjects.CreditApplicationId;
import com.smartfinance.smartfinancedriveplatform.financing.interfaces.rest.resources.CreateCreditApplicationResource;
import com.smartfinance.smartfinancedriveplatform.financing.interfaces.rest.resources.CreditApplicationResource;
import com.smartfinance.smartfinancedriveplatform.financing.interfaces.rest.resources.UpdateCreditApplicationStatusResource;
import com.smartfinance.smartfinancedriveplatform.financing.interfaces.rest.transform.CreditApplicationResourceFromEntityAssembler;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.valueobjects.Money;
import com.smartfinance.smartfinancedriveplatform.shared.infrastructure.security.SecurityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST controller for formal bank credit applications.
 */
@RestController
@RequestMapping(value = "/api/v1/credit-applications", produces = "application/json")
public class CreditApplicationsController {

    private final CreditApplicationCommandService commandService;
    private final CreditApplicationQueryService queryService;

    public CreditApplicationsController(CreditApplicationCommandService commandService,
                                        CreditApplicationQueryService queryService) {
        this.commandService = commandService;
        this.queryService = queryService;
    }

    /**
     * POST /api/v1/credit-applications
     * Submits a formal credit application for the authenticated user.
     */
    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<CreditApplicationResource> createCreditApplication(
            @jakarta.validation.Valid @RequestBody CreateCreditApplicationResource resource) {
        String authUserId = SecurityUtils.getRequiredCurrentUserId();
        var command = new CreateCreditApplicationCommand(
                authUserId,
                resource.vehicleId(),
                resource.financialEntityId(),
                resource.simulationId(),
                new Money(resource.requestedAmount(), resource.currency()),
                resource.downPayment() != null ? new Money(resource.downPayment(), resource.currency()) : new Money(BigDecimal.ZERO, resource.currency()),
                resource.termMonths(),
                new Money(resource.monthlyIncome(), resource.currency()),
                resource.employmentStatus()
        );
        var resultOpt = commandService.handle(command);
        return resultOpt
                .map(app -> new ResponseEntity<>(
                        CreditApplicationResourceFromEntityAssembler.toResourceFromEntity(app),
                        HttpStatus.CREATED
                ))
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    /**
     * GET /api/v1/credit-applications/me
     * Retrieves all formal credit applications of the authenticated buyer.
     */
    @GetMapping("/me")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<CreditApplicationResource>> getMyCreditApplications() {
        String authUserId = SecurityUtils.getRequiredCurrentUserId();
        var query = new GetCreditApplicationsByApplicantQuery(authUserId);
        var applications = queryService.handle(query);
        var resources = applications.stream()
                .map(CreditApplicationResourceFromEntityAssembler::toResourceFromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(resources);
    }

    /**
     * GET /api/v1/credit-applications/{id}
     * Retrieves a credit application details by UUID.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'FINANCIAL_INSTITUTION', 'ADMIN', 'DEALER')")
    public ResponseEntity<CreditApplicationResource> getCreditApplicationById(@PathVariable UUID id) {
        var query = new GetCreditApplicationByIdQuery(new CreditApplicationId(id));
        var appOpt = queryService.handle(query);
        return appOpt
                .map(app -> ResponseEntity.ok(CreditApplicationResourceFromEntityAssembler.toResourceFromEntity(app)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * PATCH /api/v1/credit-applications/{id}/status
     * Updates status of a formal credit application (e.g. PRE_APPROVED, REJECTED, DISBURSED).
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('FINANCIAL_INSTITUTION', 'ADMIN')")
    public ResponseEntity<CreditApplicationResource> updateCreditApplicationStatus(
            @PathVariable UUID id,
            @jakarta.validation.Valid @RequestBody UpdateCreditApplicationStatusResource resource) {
        var command = new UpdateCreditApplicationStatusCommand(
                new CreditApplicationId(id),
                resource.status(),
                resource.notes()
        );
        var updatedOpt = commandService.handle(command);
        return updatedOpt
                .map(app -> ResponseEntity.ok(CreditApplicationResourceFromEntityAssembler.toResourceFromEntity(app)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
