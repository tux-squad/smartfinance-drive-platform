package com.smartfinance.smartfinancedriveplatform.catalog.interfaces.rest;

import com.smartfinance.smartfinancedriveplatform.catalog.application.commandservices.VehicleCommandService;
import com.smartfinance.smartfinancedriveplatform.catalog.application.queryservices.VehicleQueryService;
import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.aggregates.Vehicle;
import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.commands.CreateVehicleCommand;
import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.queries.GetVehicleByIdQuery;
import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.queries.GetVehiclesByUserIdQuery;
import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.valueobjects.FinancialEntityId;
import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.valueobjects.UserId;
import com.smartfinance.smartfinancedriveplatform.catalog.interfaces.rest.resources.CreateVehicleResource;
import com.smartfinance.smartfinancedriveplatform.catalog.interfaces.rest.resources.VehicleResource;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.valueobjects.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

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

    private VehiclesController vehiclesController;

    @BeforeEach
    void setUp() {
        vehiclesController = new VehiclesController(vehicleCommandService, vehicleQueryService);
    }

    @Test
    void testCreateVehicleSuccess() {
        UUID userId = UUID.randomUUID();
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
        UUID userId = UUID.randomUUID();
        when(vehicleQueryService.handle(any(GetVehiclesByUserIdQuery.class))).thenReturn(Collections.emptyList());

        ResponseEntity<List<VehicleResource>> response = vehiclesController.getVehiclesByUserId(userId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
    }
}
