package com.smartfinance.smartfinancedriveplatform.crm.interfaces.rest;

import com.smartfinance.smartfinancedriveplatform.crm.application.commandservices.CrmCommandService;
import com.smartfinance.smartfinancedriveplatform.crm.application.queryservices.CrmQueryService;
import com.smartfinance.smartfinancedriveplatform.crm.domain.model.commands.*;
import com.smartfinance.smartfinancedriveplatform.crm.domain.model.queries.GetProspectByIdQuery;
import com.smartfinance.smartfinancedriveplatform.crm.domain.model.queries.GetProspectsForDealerQuery;
import com.smartfinance.smartfinancedriveplatform.crm.domain.model.queries.GetTestDriveByIdQuery;
import com.smartfinance.smartfinancedriveplatform.crm.domain.model.queries.GetTestDrivesForUserQuery;
import com.smartfinance.smartfinancedriveplatform.crm.domain.model.valueobjects.ProspectId;
import com.smartfinance.smartfinancedriveplatform.crm.domain.model.valueobjects.TestDriveId;
import com.smartfinance.smartfinancedriveplatform.crm.interfaces.rest.resources.*;
import com.smartfinance.smartfinancedriveplatform.crm.interfaces.rest.transform.ProspectResourceFromEntityAssembler;
import com.smartfinance.smartfinancedriveplatform.crm.interfaces.rest.transform.TestDriveResourceFromEntityAssembler;
import com.smartfinance.smartfinancedriveplatform.shared.infrastructure.security.SecurityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST Controller for CRM prospects, notes timeline, and test drive scheduling.
 */
@RestController
@RequestMapping(produces = "application/json")
public class CrmController {

    private final CrmCommandService commandService;
    private final CrmQueryService queryService;

    public CrmController(CrmCommandService commandService, CrmQueryService queryService) {
        this.commandService = commandService;
        this.queryService = queryService;
    }

    /**
     * POST /api/v1/dealers/me/prospects or /api/v1/prospects
     * Creates a new prospect.
     */
    @PostMapping({"/api/v1/dealers/me/prospects", "/api/v1/prospects"})
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ProspectResource> createProspect(
            @jakarta.validation.Valid @RequestBody CreateProspectResource resource) {
        String authUserId = SecurityUtils.getRequiredCurrentUserId();
        var command = new CreateProspectCommand(
                authUserId,
                null,
                resource.fullName(),
                resource.email(),
                resource.phone(),
                resource.interestedVehicleId(),
                resource.salesAgentId()
        );
        var prospectOpt = commandService.handle(command);
        return prospectOpt
                .map(p -> new ResponseEntity<>(ProspectResourceFromEntityAssembler.toResourceFromEntity(p), HttpStatus.CREATED))
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    /**
     * GET /api/v1/dealers/me/prospects
     * Retrieves all prospects assigned to the authenticated dealer.
     */
    @GetMapping("/api/v1/dealers/me/prospects")
    @PreAuthorize("hasAnyRole('DEALER', 'ADMIN')")
    public ResponseEntity<List<ProspectResource>> getMyProspects() {
        String authDealerId = SecurityUtils.getRequiredCurrentUserId();
        var query = new GetProspectsForDealerQuery(authDealerId);
        var prospects = queryService.handle(query);
        var resources = prospects.stream()
                .map(ProspectResourceFromEntityAssembler::toResourceFromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(resources);
    }

    /**
     * GET /api/v1/dealers/me/prospects/{id}
     * Retrieves detail of a specific prospect.
     */
    @GetMapping("/api/v1/dealers/me/prospects/{id}")
    @PreAuthorize("hasAnyRole('DEALER', 'ADMIN')")
    public ResponseEntity<ProspectResource> getProspectById(@PathVariable UUID id) {
        var query = new GetProspectByIdQuery(new ProspectId(id));
        var prospectOpt = queryService.handle(query);
        return prospectOpt
                .map(p -> ResponseEntity.ok(ProspectResourceFromEntityAssembler.toResourceFromEntity(p)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * POST /api/v1/prospects/{id}/notes
     * Adds a note to a prospect interaction timeline.
     */
    @PostMapping("/api/v1/prospects/{id}/notes")
    @PreAuthorize("hasAnyRole('DEALER', 'ADMIN')")
    public ResponseEntity<ProspectNoteResource> addProspectNote(
            @PathVariable UUID id,
            @jakarta.validation.Valid @RequestBody AddProspectNoteResource resource) {
        String authUserId = SecurityUtils.getRequiredCurrentUserId();
        var command = new AddProspectNoteCommand(new ProspectId(id), authUserId, resource.noteText());
        var noteOpt = commandService.handle(command);
        return noteOpt
                .map(n -> new ResponseEntity<>(
                        new ProspectNoteResource(n.getId(), n.getProspectId(), n.getAuthorUserId(), n.getNoteText(), n.getCreatedAt()),
                        HttpStatus.CREATED
                ))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * GET /api/v1/prospects/{id}/timeline
     * Retrieves notes timeline of a prospect.
     */
    @GetMapping("/api/v1/prospects/{id}/timeline")
    @PreAuthorize("hasAnyRole('DEALER', 'ADMIN')")
    public ResponseEntity<List<ProspectNoteResource>> getProspectTimeline(@PathVariable UUID id) {
        var query = new GetProspectByIdQuery(new ProspectId(id));
        var prospectOpt = queryService.handle(query);
        if (prospectOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        var prospect = prospectOpt.get();
        var notesResources = prospect.getNotes().stream()
                .map(n -> new ProspectNoteResource(n.getId(), n.getProspectId(), n.getAuthorUserId(), n.getNoteText(), n.getCreatedAt()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(notesResources);
    }

    /**
     * PATCH /api/v1/prospects/{id}/status
     * Updates prospect CRM status (NEW, CONTACTED, TEST_DRIVE_SCHEDULED, NEGOTIATING, CLOSED_WON, CLOSED_LOST).
     */
    @PatchMapping("/api/v1/prospects/{id}/status")
    @PreAuthorize("hasAnyRole('DEALER', 'ADMIN')")
    public ResponseEntity<ProspectResource> updateProspectStatus(
            @PathVariable UUID id,
            @jakarta.validation.Valid @RequestBody UpdateProspectStatusResource resource) {
        var command = new UpdateProspectStatusCommand(new ProspectId(id), resource.status());
        var updatedOpt = commandService.handle(command);
        return updatedOpt
                .map(p -> ResponseEntity.ok(ProspectResourceFromEntityAssembler.toResourceFromEntity(p)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * POST /api/v1/test-drives
     * Schedules a vehicle test drive.
     */
    @PostMapping("/api/v1/test-drives")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TestDriveResource> scheduleTestDrive(
            @jakarta.validation.Valid @RequestBody ScheduleTestDriveResource resource) {
        String authUserId = SecurityUtils.getRequiredCurrentUserId();
        var command = new ScheduleTestDriveCommand(
                authUserId,
                resource.vehicleId(),
                resource.dealershipId(),
                resource.scheduledDateTime(),
                resource.notes()
        );
        var testDriveOpt = commandService.handle(command);
        return testDriveOpt
                .map(td -> new ResponseEntity<>(TestDriveResourceFromEntityAssembler.toResourceFromEntity(td), HttpStatus.CREATED))
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    /**
     * GET /api/v1/test-drives/me
     * Lists test drives requested by or assigned to the authenticated user.
     */
    @GetMapping("/api/v1/test-drives/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TestDriveResource>> getMyTestDrives() {
        String authUserId = SecurityUtils.getRequiredCurrentUserId();
        var query = new GetTestDrivesForUserQuery(authUserId);
        var testDrives = queryService.handle(query);
        var resources = testDrives.stream()
                .map(TestDriveResourceFromEntityAssembler::toResourceFromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(resources);
    }

    /**
     * GET /api/v1/test-drives/{id}
     * Retrieves detail of a test drive by ID.
     */
    @GetMapping("/api/v1/test-drives/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TestDriveResource> getTestDriveById(@PathVariable UUID id) {
        var query = new GetTestDriveByIdQuery(new TestDriveId(id));
        var testDriveOpt = queryService.handle(query);
        return testDriveOpt
                .map(td -> ResponseEntity.ok(TestDriveResourceFromEntityAssembler.toResourceFromEntity(td)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * PATCH /api/v1/test-drives/{id}/status
     * Updates test drive status.
     */
    @PatchMapping("/api/v1/test-drives/{id}/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TestDriveResource> updateTestDriveStatus(
            @PathVariable UUID id,
            @jakarta.validation.Valid @RequestBody UpdateTestDriveStatusResource resource) {
        var command = new UpdateTestDriveStatusCommand(new TestDriveId(id), resource.status());
        var updatedOpt = commandService.handle(command);
        return updatedOpt
                .map(td -> ResponseEntity.ok(TestDriveResourceFromEntityAssembler.toResourceFromEntity(td)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * DELETE /api/v1/test-drives/{id}
     * Cancels / deletes a test drive appointment.
     */
    @DeleteMapping("/api/v1/test-drives/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> cancelTestDrive(@PathVariable UUID id) {
        var command = new CancelTestDriveCommand(new TestDriveId(id));
        commandService.handle(command);
        return ResponseEntity.noContent().build();
    }
}

