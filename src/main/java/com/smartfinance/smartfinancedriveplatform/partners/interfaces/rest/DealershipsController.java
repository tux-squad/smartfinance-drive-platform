package com.smartfinance.smartfinancedriveplatform.partners.interfaces.rest;

import com.smartfinance.smartfinancedriveplatform.catalog.application.queryservices.VehicleQueryService;
import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.queries.GetVehiclesByUserIdQuery;
import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.valueobjects.UserId;
import com.smartfinance.smartfinancedriveplatform.catalog.interfaces.rest.resources.VehicleResource;
import com.smartfinance.smartfinancedriveplatform.catalog.interfaces.rest.transform.VehicleResourceFromEntityAssembler;
import com.smartfinance.smartfinancedriveplatform.partners.application.commandservices.DealershipCommandService;
import com.smartfinance.smartfinancedriveplatform.partners.application.outboundservices.storage.DealershipImageStorageService;
import com.smartfinance.smartfinancedriveplatform.partners.application.queryservices.DealershipQueryService;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.commands.CreateDealershipCommand;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.queries.GetAllDealershipsQuery;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.queries.GetDealershipByIdQuery;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.queries.GetDealershipByUserIdQuery;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.valueobjects.DealershipId;
import com.smartfinance.smartfinancedriveplatform.partners.interfaces.rest.resources.CreateUpdateDealershipResource;
import com.smartfinance.smartfinancedriveplatform.partners.interfaces.rest.resources.DealershipResource;
import com.smartfinance.smartfinancedriveplatform.partners.interfaces.rest.transform.DealershipResourceFromEntityAssembler;
import com.smartfinance.smartfinancedriveplatform.shared.infrastructure.security.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST controller for managing Dealership profiles and B2B public directory.
 */
@RestController
@RequestMapping(value = "/api/v1/dealerships", produces = "application/json")
public class DealershipsController {

    private final DealershipCommandService dealershipCommandService;
    private final DealershipQueryService dealershipQueryService;
    private final DealershipImageStorageService imageStorageService;
    private final VehicleQueryService vehicleQueryService;

    public DealershipsController(DealershipCommandService dealershipCommandService,
                                DealershipQueryService dealershipQueryService,
                                DealershipImageStorageService imageStorageService,
                                VehicleQueryService vehicleQueryService) {
        this.dealershipCommandService = dealershipCommandService;
        this.dealershipQueryService = dealershipQueryService;
        this.imageStorageService = imageStorageService;
        this.vehicleQueryService = vehicleQueryService;
    }

    /**
     * GET /api/v1/dealerships
     * Public directory of active dealerships.
     */
    @GetMapping
    public ResponseEntity<Page<DealershipResource>> getAllDealerships(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 10, sort = "name") Pageable pageable) {
        var query = new GetAllDealershipsQuery(search);
        var page = dealershipQueryService.handle(query, pageable);
        return ResponseEntity.ok(page.map(DealershipResourceFromEntityAssembler::toResourceFromEntity));
    }

    /**
     * GET /api/v1/dealerships/me
     * Retrieves current authenticated dealer's profile.
     */
    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('DEALER', 'ADMIN')")
    public ResponseEntity<DealershipResource> getMyDealership() {
        String authUserId = SecurityUtils.getRequiredCurrentUserId();
        var query = new GetDealershipByUserIdQuery(authUserId);
        var dealershipOpt = dealershipQueryService.handle(query);
        return dealershipOpt
                .map(d -> ResponseEntity.ok(DealershipResourceFromEntityAssembler.toResourceFromEntity(d)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * PUT /api/v1/dealerships/me
     * Creates or updates current authenticated dealer's profile.
     */
    @PutMapping("/me")
    @PreAuthorize("hasAnyRole('DEALER', 'ADMIN')")
    public ResponseEntity<DealershipResource> createOrUpdateMyDealership(
            @jakarta.validation.Valid @RequestBody CreateUpdateDealershipResource resource) {
        String authUserId = SecurityUtils.getRequiredCurrentUserId();
        var command = new CreateDealershipCommand(
                authUserId,
                resource.ruc(),
                resource.name(),
                resource.address(),
                resource.phone(),
                resource.email(),
                resource.website(),
                resource.description(),
                resource.operatingHours(),
                resource.logoUrl(),
                resource.bannerUrl()
        );
        var resultOpt = dealershipCommandService.handle(command);
        return resultOpt
                .map(d -> ResponseEntity.ok(DealershipResourceFromEntityAssembler.toResourceFromEntity(d)))
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    /**
     * POST /api/v1/dealerships/me/logo
     * Uploads logo image for current authenticated dealer.
     */
    @PostMapping(value = "/me/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('DEALER', 'ADMIN')")
    public ResponseEntity<DealershipResource> uploadLogo(@RequestParam("file") MultipartFile file) {
        String authUserId = SecurityUtils.getRequiredCurrentUserId();
        var dealershipOpt = dealershipQueryService.handle(new GetDealershipByUserIdQuery(authUserId));
        if (dealershipOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var dealership = dealershipOpt.get();
        if (dealership.getLogoUrl() != null && !dealership.getLogoUrl().isBlank()) {
            imageStorageService.deleteDealershipImage(dealership.getLogoUrl());
        }

        String logoUrl = imageStorageService.uploadDealershipImage(file, "logos");
        var updatedOpt = dealershipCommandService.updateLogo(dealership.getId(), logoUrl);
        return updatedOpt
                .map(d -> ResponseEntity.ok(DealershipResourceFromEntityAssembler.toResourceFromEntity(d)))
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    /**
     * POST /api/v1/dealerships/me/banner
     * Uploads banner image for current authenticated dealer.
     */
    @PostMapping(value = "/me/banner", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('DEALER', 'ADMIN')")
    public ResponseEntity<DealershipResource> uploadBanner(@RequestParam("file") MultipartFile file) {
        String authUserId = SecurityUtils.getRequiredCurrentUserId();
        var dealershipOpt = dealershipQueryService.handle(new GetDealershipByUserIdQuery(authUserId));
        if (dealershipOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var dealership = dealershipOpt.get();
        if (dealership.getBannerUrl() != null && !dealership.getBannerUrl().isBlank()) {
            imageStorageService.deleteDealershipImage(dealership.getBannerUrl());
        }

        String bannerUrl = imageStorageService.uploadDealershipImage(file, "banners");
        var updatedOpt = dealershipCommandService.updateBanner(dealership.getId(), bannerUrl);
        return updatedOpt
                .map(d -> ResponseEntity.ok(DealershipResourceFromEntityAssembler.toResourceFromEntity(d)))
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    /**
     * GET /api/v1/dealerships/{id}
     * Retrieves dealership profile by UUID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<DealershipResource> getDealershipById(@PathVariable UUID id) {
        var query = new GetDealershipByIdQuery(new DealershipId(id));
        var dealershipOpt = dealershipQueryService.handle(query);
        return dealershipOpt
                .map(d -> ResponseEntity.ok(DealershipResourceFromEntityAssembler.toResourceFromEntity(d)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * GET /api/v1/dealerships/{id}/vehicles
     * Retrieves vehicles published by a specific dealership.
     */
    @GetMapping("/{id}/vehicles")
    public ResponseEntity<List<VehicleResource>> getDealershipVehicles(@PathVariable UUID id) {
        var dealershipOpt = dealershipQueryService.handle(new GetDealershipByIdQuery(new DealershipId(id)));
        if (dealershipOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var dealership = dealershipOpt.get();
        var vehicles = vehicleQueryService.handle(new GetVehiclesByUserIdQuery(new UserId(dealership.getUserId())));
        var resources = vehicles.stream()
                .map(VehicleResourceFromEntityAssembler::toResourceFromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(resources);
    }
}
