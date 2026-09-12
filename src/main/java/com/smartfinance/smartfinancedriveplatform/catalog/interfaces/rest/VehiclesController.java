package com.smartfinance.smartfinancedriveplatform.catalog.interfaces.rest;

import com.smartfinance.smartfinancedriveplatform.catalog.application.commandservices.VehicleCommandService;
import com.smartfinance.smartfinancedriveplatform.catalog.application.outboundservices.storage.VehicleImageStorageService;
import com.smartfinance.smartfinancedriveplatform.catalog.application.queryservices.VehicleQueryService;
import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.commands.DeleteVehicleCommand;
import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.commands.UpdateVehicleCommand;
import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.queries.GetVehicleByIdQuery;
import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.queries.GetVehiclesByUserIdQuery;
import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.valueobjects.UserId;
import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.valueobjects.VehicleId;
import com.smartfinance.smartfinancedriveplatform.catalog.interfaces.rest.resources.CreateVehicleResource;
import com.smartfinance.smartfinancedriveplatform.catalog.interfaces.rest.resources.UpdateVehicleResource;
import com.smartfinance.smartfinancedriveplatform.catalog.interfaces.rest.resources.VehicleResource;
import com.smartfinance.smartfinancedriveplatform.catalog.interfaces.rest.transform.CreateVehicleCommandFromResourceAssembler;
import com.smartfinance.smartfinancedriveplatform.catalog.interfaces.rest.transform.UpdateVehicleCommandFromResourceAssembler;
import com.smartfinance.smartfinancedriveplatform.catalog.interfaces.rest.transform.VehicleResourceFromEntityAssembler;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
     * POST /api/v1/vehicles
     * Registers a new vehicle in the catalog.
     *
     * @param resource The payload resource.
     * @return The response payload of the created vehicle.
     */
    @PostMapping
    public ResponseEntity<VehicleResource> createVehicle(@RequestBody CreateVehicleResource resource) {
        var command = CreateVehicleCommandFromResourceAssembler.toCommandFromResource(resource);
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
     * Retrieves vehicle details by identifier.
     *
     * @param vehicleId The vehicle UUID.
     * @return The vehicle resource payload.
     */
    @GetMapping("/{vehicleId}")
    public ResponseEntity<VehicleResource> getVehicleById(@PathVariable UUID vehicleId) {
        var query = new GetVehicleByIdQuery(new VehicleId(vehicleId));
        var vehicleOpt = vehicleQueryService.handle(query);
        return vehicleOpt
                .map(vehicle -> ResponseEntity.ok(VehicleResourceFromEntityAssembler.toResourceFromEntity(vehicle)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * GET /api/v1/vehicles/users/{userId}
     * Retrieves all vehicles belonging to a specific user.
     *
     * @param userId The user UUID.
     * @return A list of vehicle resource payloads.
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<List<VehicleResource>> getVehiclesByUserId(@PathVariable UUID userId) {
        var query = new GetVehiclesByUserIdQuery(new UserId(userId));
        var vehicles = vehicleQueryService.handle(query);
        var resources = vehicles.stream()
                .map(VehicleResourceFromEntityAssembler::toResourceFromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(resources);
    }

    /**
     * PUT /api/v1/vehicles/{vehicleId}
     * Updates an existing vehicle details.
     *
     * @param vehicleId The vehicle UUID.
     * @param resource  The update payload resource.
     * @return The updated vehicle resource.
     */
    @PutMapping("/{vehicleId}")
    public ResponseEntity<VehicleResource> updateVehicle(
            @PathVariable UUID vehicleId,
            @RequestBody UpdateVehicleResource resource) {
        var command = UpdateVehicleCommandFromResourceAssembler.toCommandFromResource(vehicleId, resource);
        var vehicleOpt = vehicleCommandService.handle(command);
        return vehicleOpt
                .map(vehicle -> ResponseEntity.ok(VehicleResourceFromEntityAssembler.toResourceFromEntity(vehicle)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * DELETE /api/v1/vehicles/{vehicleId}
     * Deletes a vehicle from the catalog.
     *
     * @param vehicleId The vehicle UUID.
     * @return 204 No Content.
     */
    @DeleteMapping("/{vehicleId}")
    public ResponseEntity<?> deleteVehicle(@PathVariable UUID vehicleId) {
        var command = new DeleteVehicleCommand(new VehicleId(vehicleId));
        vehicleCommandService.handle(command);
        return ResponseEntity.noContent().build();
    }

    /**
     * POST /api/v1/vehicles/{vehicleId}/image
     * Uploads an image for an existing vehicle to Cloudinary and updates its imagePath.
     *
     * @param vehicleId The vehicle UUID.
     * @param file      The multipart image file.
     * @return The updated vehicle resource.
     */
    @PostMapping(value = "/{vehicleId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
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
                imageUrl
        );

        var updatedOpt = vehicleCommandService.handle(updateCommand);
        return updatedOpt
                .map(v -> ResponseEntity.ok(VehicleResourceFromEntityAssembler.toResourceFromEntity(v)))
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }
}

