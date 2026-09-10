package com.smartfinance.smartfinancedriveplatform.financing.interfaces.rest;

import com.smartfinance.smartfinancedriveplatform.financing.application.commandservices.SimulationCommandService;
import com.smartfinance.smartfinancedriveplatform.financing.application.queryservices.SimulationQueryService;
import com.smartfinance.smartfinancedriveplatform.financing.domain.model.aggregates.Simulation;
import com.smartfinance.smartfinancedriveplatform.financing.domain.model.commands.CreateSimulationCommand;
import com.smartfinance.smartfinancedriveplatform.financing.domain.model.commands.DeleteSimulationCommand;
import com.smartfinance.smartfinancedriveplatform.financing.domain.model.queries.GetAllSimulationsQuery;
import com.smartfinance.smartfinancedriveplatform.financing.domain.model.valueobjects.GracePeriodType;
import com.smartfinance.smartfinancedriveplatform.financing.domain.model.valueobjects.VehicleInsuranceType;
import com.smartfinance.smartfinancedriveplatform.financing.interfaces.rest.resources.CreateSimulationResource;
import com.smartfinance.smartfinancedriveplatform.financing.interfaces.rest.resources.SimulationResource;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.valueobjects.Money;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.valueobjects.Percent;
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
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SimulationsController REST Unit Tests")
class SimulationsControllerTest {

    @Mock
    private SimulationCommandService simulationCommandService;

    @Mock
    private SimulationQueryService simulationQueryService;

    @InjectMocks
    private SimulationsController simulationsController;

    private Simulation sampleSimulation;

    @BeforeEach
    void setUp() {
        sampleSimulation = new Simulation(
                "RAV4 BCP 2026",
                "usr-1",
                "veh-1",
                "bank-1",
                Money.of(25000.0, "USD"),
                Percent.of(20.0),
                Percent.of(0.0),
                Percent.of(12.0),
                Percent.of(0.05),
                Money.of(40.0, "USD"),
                VehicleInsuranceType.MENSUAL,
                24,
                GracePeriodType.NONE,
                0,
                Money.of(100.0, "USD"),
                Percent.of(10.0),
                LocalDate.now()
        );
    }

    @Test
    @DisplayName("Should create simulation and return 201 Created with full payload")
    void shouldCreateSimulation() {
        CreateSimulationResource resource = new CreateSimulationResource(
                "RAV4 BCP 2026",
                "usr-1",
                "veh-1",
                "bank-1",
                new BigDecimal("25000.00"),
                "USD",
                new BigDecimal("20.00"),
                new BigDecimal("0.00"),
                new BigDecimal("12.00"),
                new BigDecimal("0.05"),
                new BigDecimal("40.00"),
                "MENSUAL",
                24,
                "NONE",
                0,
                new BigDecimal("100.00"),
                new BigDecimal("10.00"),
                LocalDate.now()
        );

        when(simulationCommandService.handle(any(CreateSimulationCommand.class))).thenReturn(Optional.of(sampleSimulation));

        ResponseEntity<SimulationResource> response = simulationsController.createSimulation(resource);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("RAV4 BCP 2026", response.getBody().title());
        assertEquals(24, response.getBody().paymentSchedule().size());
    }

    @Test
    @DisplayName("Should get all simulations and return 200 OK")
    void shouldGetAllSimulations() {
        when(simulationQueryService.handle(any(GetAllSimulationsQuery.class))).thenReturn(List.of(sampleSimulation));

        ResponseEntity<List<SimulationResource>> response = simulationsController.getAllSimulations();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    @DisplayName("Should delete simulation and return 204 No Content")
    void shouldDeleteSimulation() {
        UUID simulationId = UUID.randomUUID();
        doNothing().when(simulationCommandService).handle(any(DeleteSimulationCommand.class));

        ResponseEntity<?> response = simulationsController.deleteSimulation(simulationId);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(simulationCommandService, times(1)).handle(any(DeleteSimulationCommand.class));
    }
}
