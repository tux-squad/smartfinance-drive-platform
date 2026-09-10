package com.smartfinance.smartfinancedriveplatform.financing.infrastructure.persistence.jpa.adapters;

import com.smartfinance.smartfinancedriveplatform.financing.domain.model.aggregates.Simulation;
import com.smartfinance.smartfinancedriveplatform.financing.domain.model.valueobjects.GracePeriodType;
import com.smartfinance.smartfinancedriveplatform.financing.domain.model.valueobjects.SimulationId;
import com.smartfinance.smartfinancedriveplatform.financing.domain.model.valueobjects.VehicleInsuranceType;
import com.smartfinance.smartfinancedriveplatform.financing.infrastructure.persistence.jpa.entities.SimulationPersistenceEntity;
import com.smartfinance.smartfinancedriveplatform.financing.infrastructure.persistence.jpa.repositories.SpringDataSimulationRepository;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.valueobjects.Money;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.valueobjects.Percent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SimulationRepositoryAdapter Tests")
class SimulationRepositoryAdapterTest {

    @Mock
    private SpringDataSimulationRepository springDataSimulationRepository;

    @InjectMocks
    private SimulationRepositoryAdapter repositoryAdapter;

    private Simulation sampleSimulation;

    @BeforeEach
    void setUp() {
        sampleSimulation = new Simulation(
                "Simulacion Test",
                "user-1",
                "veh-1",
                "entity-1",
                Money.of(20000.0, "USD"),
                Percent.of(20.0),
                Percent.of(0.0),
                Percent.of(12.0),
                Percent.of(0.05),
                Money.of(40.0, "USD"),
                VehicleInsuranceType.MENSUAL,
                12,
                GracePeriodType.NONE,
                0,
                Money.of(100.0, "USD"),
                Percent.of(10.0),
                LocalDate.now()
        );
    }

    @Test
    @DisplayName("Should save simulation successfully via Spring Data JPA")
    void shouldSaveSimulationSuccessfully() {
        when(springDataSimulationRepository.findById(any())).thenReturn(Optional.empty());
        when(springDataSimulationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Simulation saved = repositoryAdapter.save(sampleSimulation);

        assertNotNull(saved);
        assertEquals("Simulacion Test", saved.getTitle());
        verify(springDataSimulationRepository, times(1)).save(any(SimulationPersistenceEntity.class));
    }

    @Test
    @DisplayName("Should find simulation by ID")
    void shouldFindSimulationById() {
        SimulationId simulationId = sampleSimulation.getId();
        when(springDataSimulationRepository.findById(simulationId.value())).thenReturn(Optional.empty());

        Optional<Simulation> result = repositoryAdapter.findById(simulationId);

        assertTrue(result.isEmpty());
        verify(springDataSimulationRepository, times(1)).findById(simulationId.value());
    }
}
