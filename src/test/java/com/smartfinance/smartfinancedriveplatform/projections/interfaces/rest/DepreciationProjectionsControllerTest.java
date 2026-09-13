package com.smartfinance.smartfinancedriveplatform.projections.interfaces.rest;

import com.smartfinance.smartfinancedriveplatform.projections.application.commandservices.DepreciationProjectionCommandService;
import com.smartfinance.smartfinancedriveplatform.projections.application.queryservices.DepreciationProjectionQueryService;
import com.smartfinance.smartfinancedriveplatform.projections.domain.model.aggregates.DepreciationProjection;
import com.smartfinance.smartfinancedriveplatform.projections.domain.model.commands.CalculateDepreciationProjectionCommand;
import com.smartfinance.smartfinancedriveplatform.projections.domain.model.commands.DeleteDepreciationProjectionCommand;
import com.smartfinance.smartfinancedriveplatform.projections.domain.model.queries.GetAllDepreciationProjectionsQuery;
import com.smartfinance.smartfinancedriveplatform.projections.domain.model.queries.GetDepreciationProjectionsByVehicleIdQuery;
import com.smartfinance.smartfinancedriveplatform.projections.domain.model.valueobjects.MotorizationType;
import com.smartfinance.smartfinancedriveplatform.projections.interfaces.rest.resources.CalculateDepreciationProjectionResource;
import com.smartfinance.smartfinancedriveplatform.projections.interfaces.rest.resources.DepreciationProjectionResource;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.valueobjects.Money;
import com.smartfinance.smartfinancedriveplatform.shared.infrastructure.security.OwnershipChecker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DepreciationProjectionsController REST Unit Tests")
class DepreciationProjectionsControllerTest {

    @Mock
    private DepreciationProjectionCommandService commandService;

    @Mock
    private DepreciationProjectionQueryService queryService;

    @Mock
    private OwnershipChecker ownershipChecker;

    @InjectMocks
    private DepreciationProjectionsController controller;

    private DepreciationProjection sampleProjection;

    @BeforeEach
    void setUp() {
        sampleProjection = new DepreciationProjection(
                "veh-100",
                "sim-200",
                Money.of(25000.0, "USD"),
                2025,
                MotorizationType.COMBUSTION,
                Money.of(12000.0, "USD")
        );
    }

    @Test
    @DisplayName("Should calculate projection and return 201 Created")
    void shouldCalculateProjection() {
        CalculateDepreciationProjectionResource resource = new CalculateDepreciationProjectionResource(
                "veh-100",
                "sim-200",
                new BigDecimal("25000.00"),
                "USD",
                2025,
                "COMBUSTION",
                new BigDecimal("12000.00")
        );

        when(commandService.handle(any(CalculateDepreciationProjectionCommand.class))).thenReturn(Optional.of(sampleProjection));

        ResponseEntity<DepreciationProjectionResource> response = controller.calculateProjection(resource);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("veh-100", response.getBody().vehicleId());
        assertEquals("COMBUSTION", response.getBody().motorizationType());
    }

    @Test
    @DisplayName("Should get all projections and return 200 OK")
    void shouldGetAllProjections() {
        when(queryService.handle(any(GetAllDepreciationProjectionsQuery.class), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(sampleProjection)));
        when(ownershipChecker.isDepreciationProjectionOwner(any(), any())).thenReturn(true);

        ResponseEntity<org.springframework.data.domain.Page<DepreciationProjectionResource>> response =
                controller.getAllProjections(org.springframework.data.domain.Pageable.unpaged());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getContent().size());
    }

    @Test
    @DisplayName("Should get projections by vehicle ID and return 200 OK")
    void shouldGetProjectionsByVehicleId() {
        when(queryService.handle(any(GetDepreciationProjectionsByVehicleIdQuery.class))).thenReturn(List.of(sampleProjection));

        ResponseEntity<List<DepreciationProjectionResource>> response = controller.getProjectionsByVehicleId("veh-100");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    @DisplayName("Should delete projection and return 204 No Content")
    void shouldDeleteProjection() {
        UUID projectionId = UUID.randomUUID();
        doNothing().when(commandService).handle(any(DeleteDepreciationProjectionCommand.class));

        ResponseEntity<?> response = controller.deleteProjection(projectionId);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(commandService, times(1)).handle(any(DeleteDepreciationProjectionCommand.class));
    }
}
