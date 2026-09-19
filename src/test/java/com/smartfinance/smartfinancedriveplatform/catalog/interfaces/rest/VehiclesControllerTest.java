package com.smartfinance.smartfinancedriveplatform.catalog.interfaces.rest;

import com.smartfinance.smartfinancedriveplatform.catalog.application.commandservices.VehicleCommandService;
import com.smartfinance.smartfinancedriveplatform.catalog.application.outboundservices.storage.VehicleImageStorageService;
import com.smartfinance.smartfinancedriveplatform.catalog.application.queryservices.VehicleQueryService;
import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.aggregates.Vehicle;
import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.commands.CreateVehicleCommand;
import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.commands.UpdateVehicleCommand;
import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.queries.GetVehicleByIdQuery;
import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.queries.GetVehiclesByUserIdQuery;
import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.valueobjects.FinancialEntityId;
import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.valueobjects.UserId;
import com.smartfinance.smartfinancedriveplatform.catalog.interfaces.rest.resources.CreateVehicleResource;
import com.smartfinance.smartfinancedriveplatform.catalog.interfaces.rest.resources.VehicleResource;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.valueobjects.Money;
import com.smartfinance.smartfinancedriveplatform.shared.infrastructure.security.SecurityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for VehiclesController.
 * Mocks application layer services using Mockito to test mapping and REST handlers.
 */
@ExtendWith(MockitoExtension.class)
class VehiclesControllerTest {

    @Mock
    private VehicleCommandService vehicleCommandService;

    @Mock
    private VehicleQueryService vehicleQueryService;

    @Mock
    private VehicleImageStorageService vehicleImageStorageService;

    private VehiclesController vehiclesController;

    @BeforeEach
    void setUp() {
        vehiclesController = new VehiclesController(vehicleCommandService, vehicleQueryService, vehicleImageStorageService);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testCreateVehicleSuccess() {
        String userId = UUID.randomUUID().toString();
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "testuser@example.com", "password", Collections.emptyList()
        );
        auth.setDetails(new SecurityUtils.AuthenticatedUserDetails(userId, "testuser@example.com"));
        SecurityContextHolder.getContext().setAuthentication(auth);

        UUID bankId = UUID.randomUUID();
        CreateVehicleResource resource = new CreateVehicleResource(
            userId, bankId, "Toyota", "Corolla", 2023, "NEW", BigDecimal.valueOf(15000), "USD", null
        );

        Vehicle vehicle = new Vehicle(
            new UserId(userId), new FinancialEntityId(bankId), "Toyota", "Corolla", 2023, "NEW", Money.of(15000, "USD"), null
        );

        when(vehicleCommandService.handle(any(CreateVehicleCommand.class))).thenReturn(Optional.of(vehicle));

        ResponseEntity<VehicleResource> response = vehiclesController.createVehicle(resource);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Toyota", response.getBody().brand());
        assertEquals("Corolla", response.getBody().model());
    }

    @Test
    void testGetVehicleByIdFound() {
        UUID vehicleId = UUID.randomUUID();
        Vehicle vehicle = new Vehicle(
            new UserId(UUID.randomUUID()), new FinancialEntityId(UUID.randomUUID()), "Toyota", "Corolla", 2023, "NEW", Money.of(15000, "USD"), null
        );

        when(vehicleQueryService.handle(any(GetVehicleByIdQuery.class))).thenReturn(Optional.of(vehicle));

        ResponseEntity<VehicleResource> response = vehiclesController.getVehicleById(vehicleId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testGetVehiclesByUserId() {
        String userId = UUID.randomUUID().toString();
        when(vehicleQueryService.handle(any(GetVehiclesByUserIdQuery.class))).thenReturn(Collections.emptyList());

        ResponseEntity<List<VehicleResource>> response = vehiclesController.getVehiclesByUserId(userId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void testGetMyVehicles() {
        String authUserId = UUID.randomUUID().toString();
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "dealer@example.com", "password", Collections.emptyList()
        );
        auth.setDetails(new SecurityUtils.AuthenticatedUserDetails(authUserId, "dealer@example.com"));
        SecurityContextHolder.getContext().setAuthentication(auth);

        Vehicle vehicle = new Vehicle(
            new UserId(authUserId), new FinancialEntityId(UUID.randomUUID()), "Toyota", "RAV4", 2024, "NEW", Money.of(35000, "USD"), null
        );

        when(vehicleQueryService.handle(argThat((GetVehiclesByUserIdQuery q) -> q.userId().value().equals(authUserId))))
                .thenReturn(List.of(vehicle));

        ResponseEntity<List<VehicleResource>> response = vehiclesController.getMyVehicles();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("RAV4", response.getBody().get(0).model());
    }

    @Test
    void testUploadVehicleImageSuccess() {
        UUID vehicleId = UUID.randomUUID();
        Vehicle vehicle = new Vehicle(
            new UserId(UUID.randomUUID()), new FinancialEntityId(UUID.randomUUID()), "Toyota", "Corolla", 2023, "NEW", Money.of(15000, "USD"), null
        );

        String uploadedUrl = "https://res.cloudinary.com/jzoqodzv/image/upload/v1/smartfinance/vehicles/test.jpg";
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "image content".getBytes());

        Vehicle updatedVehicle = new Vehicle(
            vehicle.getUserId(), vehicle.getFinancialEntityId(), "Toyota", "Corolla", 2023, "NEW", Money.of(15000, "USD"), uploadedUrl
        );

        when(vehicleQueryService.handle(any(GetVehicleByIdQuery.class))).thenReturn(Optional.of(vehicle));
        when(vehicleImageStorageService.uploadVehicleImage(any())).thenReturn(uploadedUrl);
        when(vehicleCommandService.handle(any(UpdateVehicleCommand.class))).thenReturn(Optional.of(updatedVehicle));

        ResponseEntity<VehicleResource> response = vehiclesController.uploadVehicleImage(vehicleId, file);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(uploadedUrl, response.getBody().imagePath());
        verify(vehicleImageStorageService, never()).deleteVehicleImage(any());
    }

    @Test
    void testUploadVehicleImageDeletesPreviousImageWhenPresent() {
        UUID vehicleId = UUID.randomUUID();
        String oldImageUrl = "https://res.cloudinary.com/jzoqodzv/image/upload/v1/smartfinance/vehicles/old_car.jpg";
        Vehicle vehicleWithOldImage = new Vehicle(
            new UserId(UUID.randomUUID()), new FinancialEntityId(UUID.randomUUID()), "Toyota", "Corolla", 2023, "NEW", Money.of(15000, "USD"), oldImageUrl
        );

        String newUploadedUrl = "https://res.cloudinary.com/jzoqodzv/image/upload/v1/smartfinance/vehicles/new_car.jpg";
        MockMultipartFile file = new MockMultipartFile("file", "new_car.jpg", "image/jpeg", "image content".getBytes());

        Vehicle updatedVehicle = new Vehicle(
            vehicleWithOldImage.getUserId(), vehicleWithOldImage.getFinancialEntityId(), "Toyota", "Corolla", 2023, "NEW", Money.of(15000, "USD"), newUploadedUrl
        );

        when(vehicleQueryService.handle(any(GetVehicleByIdQuery.class))).thenReturn(Optional.of(vehicleWithOldImage));
        when(vehicleImageStorageService.uploadVehicleImage(any())).thenReturn(newUploadedUrl);
        when(vehicleCommandService.handle(any(UpdateVehicleCommand.class))).thenReturn(Optional.of(updatedVehicle));

        ResponseEntity<VehicleResource> response = vehiclesController.uploadVehicleImage(vehicleId, file);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(newUploadedUrl, response.getBody().imagePath());

        // Verify that deleteVehicleImage was explicitly called for the old image URL
        verify(vehicleImageStorageService, times(1)).deleteVehicleImage(oldImageUrl);
    }

    @Test
    void testGetAllVehiclesSuccess() {
        Vehicle vehicle = new Vehicle(
            new UserId(UUID.randomUUID()), new FinancialEntityId(UUID.randomUUID()), "Toyota", "Corolla", 2023, "NEW", Money.of(15000, "USD"), null
        );
        org.springframework.data.domain.Page<Vehicle> page = new org.springframework.data.domain.PageImpl<>(List.of(vehicle));

        when(vehicleQueryService.handle(any(com.smartfinance.smartfinancedriveplatform.catalog.domain.model.queries.GetAllVehiclesQuery.class), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(page);

        ResponseEntity<org.springframework.data.domain.Page<VehicleResource>> response = vehiclesController.getAllVehicles(
                "Toyota", "Corolla", BigDecimal.valueOf(10000), BigDecimal.valueOf(20000), 2020, 2024, "NEW", org.springframework.data.domain.PageRequest.of(0, 10)
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());
        assertEquals("Toyota", response.getBody().getContent().get(0).brand());
    }

    @Test
    void testGetAllVehiclesEmptyFilters() {
        org.springframework.data.domain.Page<Vehicle> emptyPage = org.springframework.data.domain.Page.empty();
        when(vehicleQueryService.handle(any(), any())).thenReturn(emptyPage);

        ResponseEntity<org.springframework.data.domain.Page<VehicleResource>> response = vehiclesController.getAllVehicles(
                null, null, null, null, null, null, null, org.springframework.data.domain.PageRequest.of(0, 10)
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void testGetAllVehiclesPageSizeCappedTo50() {
        org.springframework.data.domain.Page<Vehicle> emptyPage = org.springframework.data.domain.Page.empty();
        when(vehicleQueryService.handle(any(), any())).thenReturn(emptyPage);

        ResponseEntity<org.springframework.data.domain.Page<VehicleResource>> response = vehiclesController.getAllVehicles(
                null, null, null, null, null, null, null, org.springframework.data.domain.PageRequest.of(0, 100)
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(vehicleQueryService).handle(any(), argThat(pageable -> pageable.getPageSize() == 50));
    }

    @Test
    void testGetAllVehiclesInvalidConditionThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> 
            vehiclesController.getAllVehicles(null, null, null, null, null, null, "INVALID_COND", org.springframework.data.domain.PageRequest.of(0, 10))
        );
    }
}
