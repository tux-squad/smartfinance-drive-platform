package com.smartfinance.smartfinancedriveplatform.catalog.interfaces.rest;

import com.smartfinance.smartfinancedriveplatform.catalog.application.commandservices.VehicleCommandService;
import com.smartfinance.smartfinancedriveplatform.catalog.application.outboundservices.storage.VehicleImageStorageService;
import com.smartfinance.smartfinancedriveplatform.catalog.application.queryservices.VehicleQueryService;
import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.commands.DeleteVehicleCommand;
import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.commands.UpdateVehicleCommand;
import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.commands.UpdateVehicleStatusCommand;
import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.queries.GetAllVehiclesQuery;
import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.queries.GetVehicleByIdQuery;
import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.queries.GetVehiclesByUserIdQuery;
import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.valueobjects.UserId;
import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.valueobjects.VehicleId;
import com.smartfinance.smartfinancedriveplatform.catalog.interfaces.rest.resources.CreateVehicleResource;
import com.smartfinance.smartfinancedriveplatform.catalog.interfaces.rest.resources.UpdateVehicleResource;
import com.smartfinance.smartfinancedriveplatform.catalog.interfaces.rest.resources.UpdateVehicleStatusResource;
import com.smartfinance.smartfinancedriveplatform.catalog.interfaces.rest.resources.VehicleResource;
import com.smartfinance.smartfinancedriveplatform.catalog.interfaces.rest.transform.CreateVehicleCommandFromResourceAssembler;
import com.smartfinance.smartfinancedriveplatform.catalog.interfaces.rest.transform.UpdateVehicleCommandFromResourceAssembler;
import com.smartfinance.smartfinancedriveplatform.catalog.interfaces.rest.transform.VehicleResourceFromEntityAssembler;
import com.smartfinance.smartfinancedriveplatform.shared.infrastructure.security.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST controller for managing vehicles in the catalog.
 * Follows the CQRS pattern: delegates write commands to VehicleCommandService
 * and read queries to VehicleQueryService.
 */
@RestController
@RequestMapping(value = "/api/v1/vehicles", produces = "application/json")
public class VehiclesController {

    private final VehicleCommandService vehicleCommandService;
    private final VehicleQueryService vehicleQueryService;
    private final VehicleImageStorageService vehicleImageStorageService;

    public VehiclesController(VehicleCommandService vehicleCommandService, 
                              VehicleQueryService vehicleQueryService,
                              VehicleImageStorageService vehicleImageStorageService) {
        this.vehicleCommandService = vehicleCommandService;
        this.vehicleQueryService = vehicleQueryService;
        this.vehicleImageStorageService = vehicleImageStorageService;
    }

    /**
     * GET /api/v1/vehicles/brands
     * Retrieves distinct vehicle brands currently registered in the catalog.
     *
     * @return List of brand names.
     */
    @GetMapping("/brands")
    public ResponseEntity<List<String>> getDistinctBrands() {
        List<String> brands = vehicleQueryService.getDistinctBrands();
        return ResponseEntity.ok(brands);
    }

    /**
     * GET /api/v1/vehicles
     * Retrieves all vehicles in the catalog matching optional search/filtering parameters with pagination.
     *
     * @param brand     Brand substring filter (optional).
     * @param model     Model substring filter (optional).
     * @param minPrice  Minimum price filter (optional).
     * @param maxPrice  Maximum price filter (optional).
     * @param minYear   Minimum manufacture year filter (optional).
     * @param maxYear   Maximum manufacture year filter (optional).
     * @param condition Condition filter ("NEW" or "USED") (optional).
     * @param pageable  Pagination parameters.
     * @return Page of vehicle resource payloads.
     */
    @GetMapping
    public ResponseEntity<Page<VehicleResource>> getAllVehicles(
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String model,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Integer minYear,
            @RequestParam(required = false) Integer maxYear,
            @RequestParam(required = false) String condition,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {

        if (condition != null && !condition.isBlank()) {
            String normalizedCondition = condition.trim().toUpperCase();
            if (!normalizedCondition.equals("NEW") && !normalizedCondition.equals("USED")) {
                throw new IllegalArgumentException("Invalid condition filter: '" + condition + "'. Allowed values are: NEW, USED.");
            }
        }

        Pageable cappedPageable = pageable;
        if (pageable.isPaged() && pageable.getPageSize() > 50) {
            cappedPageable = PageRequest.of(pageable.getPageNumber(), 50, pageable.getSort());
        }

        var query = new GetAllVehiclesQuery(brand, model, minPrice, maxPrice, minYear, maxYear, condition);
        var vehiclesPage = vehicleQueryService.handle(query, cappedPageable);
        var resourcePage = vehiclesPage.map(VehicleResourceFromEntityAssembler::toResourceFromEntity);
        return ResponseEntity.ok(resourcePage);
    }

    /**
     * POST /api/v1/vehicles
     * Registers a new vehicle in the catalog, strictly using the authenticated user's ID.
     *
     * @param resource The payload resource.
     * @return The response payload of the created vehicle.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DEALER')")
    public ResponseEntity<VehicleResource> createVehicle(@jakarta.validation.Valid @RequestBody CreateVehicleResource resource) {
        String authUserId = SecurityUtils.getRequiredCurrentUserId();
        var command = CreateVehicleCommandFromResourceAssembler.toCommandFromResource(resource, authUserId);
        var vehicleOpt = vehicleCommandService.handle(command);
        return vehicleOpt
                .map(vehicle -> new ResponseEntity<>(
                        VehicleResourceFromEntityAssembler.toResourceFromEntity(vehicle),
                        HttpStatus.CREATED
                ))
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    /**
     * GET /api/v1/vehicles/{vehicleId}
     * Retrieves vehicle details by identifier if owned by caller or ADMIN.
     *
     * @param vehicleId The vehicle UUID.
     * @return The vehicle resource payload.
     */
    @GetMapping("/{vehicleId}")
    @PreAuthorize("hasRole('ADMIN') or @ownershipChecker.isVehicleOwner(#vehicleId, authentication)")
    public ResponseEntity<VehicleResource> getVehicleById(@PathVariable UUID vehicleId) {
        var query = new GetVehicleByIdQuery(new VehicleId(vehicleId));
        var vehicleOpt = vehicleQueryService.handle(query);
        return vehicleOpt
                .map(vehicle -> ResponseEntity.ok(VehicleResourceFromEntityAssembler.toResourceFromEntity(vehicle)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * GET /api/v1/vehicles/users/{userId}
     * Retrieves all vehicles belonging to a specific user. Restricted to caller or ADMIN.
     *
     * @param userId The user ID string.
     * @return A list of vehicle resource payloads.
     */
    @GetMapping("/users/{userId}")
    @PreAuthorize("hasRole('ADMIN') or @ownershipChecker.isUserSelfStr(#userId, authentication)")
    public ResponseEntity<List<VehicleResource>> getVehiclesByUserId(@PathVariable String userId) {
        var query = new GetVehiclesByUserIdQuery(new UserId(userId));
        var vehicles = vehicleQueryService.handle(query);
        var resources = vehicles.stream()
                .map(VehicleResourceFromEntityAssembler::toResourceFromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(resources);
    }

    /**
     * PUT /api/v1/vehicles/{vehicleId}
     * Updates an existing vehicle details if owned by current user.
     *
     * @param vehicleId The vehicle UUID.
     * @param resource  The update payload resource.
     * @return The updated vehicle resource.
     */
    @PutMapping("/{vehicleId}")
    @PreAuthorize("@ownershipChecker.isVehicleOwner(#vehicleId, authentication)")
    public ResponseEntity<VehicleResource> updateVehicle(
            @PathVariable UUID vehicleId,
            @jakarta.validation.Valid @RequestBody UpdateVehicleResource resource) {
        var command = UpdateVehicleCommandFromResourceAssembler.toCommandFromResource(vehicleId, resource);
        var vehicleOpt = vehicleCommandService.handle(command);
        return vehicleOpt
                .map(vehicle -> ResponseEntity.ok(VehicleResourceFromEntityAssembler.toResourceFromEntity(vehicle)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * PATCH /api/v1/vehicles/{vehicleId}/status
     * Updates the status of a vehicle (ACTIVE, SOLD, RESERVED) if owned by current user.
     *
     * @param vehicleId The vehicle UUID.
     * @param resource  The status resource.
     * @return The updated vehicle resource.
     */
    @PatchMapping("/{vehicleId}/status")
    @PreAuthorize("@ownershipChecker.isVehicleOwner(#vehicleId, authentication)")
    public ResponseEntity<VehicleResource> updateVehicleStatus(
            @PathVariable UUID vehicleId,
            @jakarta.validation.Valid @RequestBody UpdateVehicleStatusResource resource) {
        var command = new UpdateVehicleStatusCommand(new VehicleId(vehicleId), resource.status());
        var vehicleOpt = vehicleCommandService.handle(command);
        return vehicleOpt
                .map(v -> ResponseEntity.ok(VehicleResourceFromEntityAssembler.toResourceFromEntity(v)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * DELETE /api/v1/vehicles/{vehicleId}
     * Deletes a vehicle from the catalog if owned by current user.
     *
     * @param vehicleId The vehicle UUID.
     * @return 204 No Content.
     */
    @DeleteMapping("/{vehicleId}")
    @PreAuthorize("@ownershipChecker.isVehicleOwner(#vehicleId, authentication)")
    public ResponseEntity<?> deleteVehicle(@PathVariable UUID vehicleId) {
        var command = new DeleteVehicleCommand(new VehicleId(vehicleId));
        vehicleCommandService.handle(command);
        return ResponseEntity.noContent().build();
    }

    /**
     * POST /api/v1/vehicles/{vehicleId}/image
     * Uploads a cover image for an existing vehicle to Cloudinary and updates its imagePath.
     *
     * @param vehicleId The vehicle UUID.
     * @param file      The multipart image file.
     * @return The updated vehicle resource.
     */
    @PostMapping(value = "/{vehicleId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@ownershipChecker.isVehicleOwner(#vehicleId, authentication)")
    public ResponseEntity<VehicleResource> uploadVehicleImage(
            @PathVariable UUID vehicleId,
            @RequestParam("file") MultipartFile file) {
        var query = new GetVehicleByIdQuery(new VehicleId(vehicleId));
        var vehicleOpt = vehicleQueryService.handle(query);
        if (vehicleOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var vehicle = vehicleOpt.get();
        if (vehicle.getImagePath() != null && !vehicle.getImagePath().isBlank()) {
            vehicleImageStorageService.deleteVehicleImage(vehicle.getImagePath());
        }
        String imageUrl = vehicleImageStorageService.uploadVehicleImage(file);

        var updateCommand = new UpdateVehicleCommand(
                vehicle.getId(),
                vehicle.getFinancialEntityId(),
                vehicle.getBrand(),
                vehicle.getModel(),
                vehicle.getManufactureYear(),
                vehicle.getCondition(),
                vehicle.getPrice(),
                imageUrl,
                vehicle.getStatus(),
                vehicle.getMileage(),
                vehicle.getTransmission(),
                vehicle.getEngine(),
                vehicle.getTraction(),
                vehicle.getImages()
        );

        var updatedOpt = vehicleCommandService.handle(updateCommand);
        return updatedOpt
                .map(v -> ResponseEntity.ok(VehicleResourceFromEntityAssembler.toResourceFromEntity(v)))
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    /**
     * POST /api/v1/vehicles/{vehicleId}/images
     * Uploads an additional image to the vehicle's photo gallery.
     *
     * @param vehicleId The vehicle UUID.
     * @param file      The multipart image file to append to gallery.
     * @return The updated vehicle resource.
     */
    @PostMapping(value = "/{vehicleId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@ownershipChecker.isVehicleOwner(#vehicleId, authentication)")
    public ResponseEntity<VehicleResource> uploadVehicleGalleryImage(
            @PathVariable UUID vehicleId,
            @RequestParam("file") MultipartFile file) {
        var query = new GetVehicleByIdQuery(new VehicleId(vehicleId));
        var vehicleOpt = vehicleQueryService.handle(query);
        if (vehicleOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        String imageUrl = vehicleImageStorageService.uploadVehicleImage(file);
        var updatedOpt = vehicleCommandService.addGalleryImage(new VehicleId(vehicleId), imageUrl);
        return updatedOpt
                .map(v -> ResponseEntity.ok(VehicleResourceFromEntityAssembler.toResourceFromEntity(v)))
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    /**
     * DELETE /api/v1/vehicles/{vehicleId}/images/{imageIndex}
     * Deletes a specific image from the vehicle's photo gallery by index.
     *
     * @param vehicleId  The vehicle UUID.
     * @param imageIndex The index of the image in gallery to delete.
     * @return The updated vehicle resource.
     */
    @DeleteMapping("/{vehicleId}/images/{imageIndex}")
    @PreAuthorize("@ownershipChecker.isVehicleOwner(#vehicleId, authentication)")
    public ResponseEntity<VehicleResource> deleteVehicleGalleryImage(
            @PathVariable UUID vehicleId,
            @PathVariable int imageIndex) {
        var query = new GetVehicleByIdQuery(new VehicleId(vehicleId));
        var vehicleOpt = vehicleQueryService.handle(query);
        if (vehicleOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var vehicle = vehicleOpt.get();
        List<String> currentImages = vehicle.getImages();
        if (currentImages == null || imageIndex < 0 || imageIndex >= currentImages.size()) {
            return ResponseEntity.badRequest().build();
        }

        String imageToRemove = currentImages.get(imageIndex);
        if (imageToRemove != null && !imageToRemove.isBlank()) {
            vehicleImageStorageService.deleteVehicleImage(imageToRemove);
        }

        List<String> updatedImages = new java.util.ArrayList<>(currentImages);
        updatedImages.remove(imageIndex);

        var updateCommand = new UpdateVehicleCommand(
                vehicle.getId(),
                vehicle.getFinancialEntityId(),
                vehicle.getBrand(),
                vehicle.getModel(),
                vehicle.getManufactureYear(),
                vehicle.getCondition(),
                vehicle.getPrice(),
                vehicle.getImagePath(),
                vehicle.getStatus(),
                vehicle.getMileage(),
                vehicle.getTransmission(),
                vehicle.getEngine(),
                vehicle.getTraction(),
                updatedImages
        );

        var updatedOpt = vehicleCommandService.handle(updateCommand);
        return updatedOpt
                .map(v -> ResponseEntity.ok(VehicleResourceFromEntityAssembler.toResourceFromEntity(v)))
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }
}


