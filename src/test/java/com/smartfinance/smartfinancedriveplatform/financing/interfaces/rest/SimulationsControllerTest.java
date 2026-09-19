package com.smartfinance.smartfinancedriveplatform.financing.interfaces.rest;

import com.smartfinance.smartfinancedriveplatform.financing.application.commandservices.CreditApplicationCommandService;
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
import com.smartfinance.smartfinancedriveplatform.shared.infrastructure.security.SecurityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
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

    @Mock
    private CreditApplicationCommandService creditApplicationCommandService;

    @InjectMocks
    private SimulationsController simulationsController;

    private Simulation sampleSimulation;

    @BeforeEach
    void setUp() {
        sampleSimulation = new Simulation(
                "RAV4 BCP 2026",
                "usr-1",
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
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

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should create simulation and return 201 Created with full payload")
    void shouldCreateSimulation() {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "usr-1", "password", Collections.emptyList()
        );
        auth.setDetails(new SecurityUtils.AuthenticatedUserDetails("usr-1", "usr-1"));
        SecurityContextHolder.getContext().setAuthentication(auth);

        CreateSimulationResource resource = new CreateSimulationResource(
                "RAV4 BCP 2026",
                "usr-1",
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
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
        when(simulationQueryService.handle(any(GetAllSimulationsQuery.class), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(sampleSimulation)));

        ResponseEntity<org.springframework.data.domain.Page<SimulationResource>> response =
                simulationsController.getAllSimulations(org.springframework.data.domain.Pageable.unpaged());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getContent().size());
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
