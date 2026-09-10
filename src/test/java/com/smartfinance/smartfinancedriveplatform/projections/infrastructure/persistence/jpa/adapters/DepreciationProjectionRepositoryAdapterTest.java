package com.smartfinance.smartfinancedriveplatform.projections.infrastructure.persistence.jpa.adapters;

import com.smartfinance.smartfinancedriveplatform.projections.domain.model.aggregates.DepreciationProjection;
import com.smartfinance.smartfinancedriveplatform.projections.domain.model.valueobjects.MotorizationType;
import com.smartfinance.smartfinancedriveplatform.projections.infrastructure.persistence.jpa.entities.DepreciationProjectionPersistenceEntity;
import com.smartfinance.smartfinancedriveplatform.projections.infrastructure.persistence.jpa.repositories.SpringDataDepreciationProjectionRepository;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.valueobjects.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DepreciationProjectionRepositoryAdapter Tests")
class DepreciationProjectionRepositoryAdapterTest {

    @Mock
    private SpringDataDepreciationProjectionRepository repository;

    @InjectMocks
    private DepreciationProjectionRepositoryAdapter adapter;

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
    @DisplayName("Should save depreciation projection via Spring Data JPA")
    void shouldSaveDepreciationProjection() {
        when(repository.findById(any())).thenReturn(Optional.empty());
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        DepreciationProjection saved = adapter.save(sampleProjection);

        assertNotNull(saved);
        assertEquals("veh-100", saved.getVehicleId());
        verify(repository, times(1)).save(any(DepreciationProjectionPersistenceEntity.class));
    }

    @Test
    @DisplayName("Should find projections by vehicleId")
    void shouldFindProjectionsByVehicleId() {
        when(repository.findByVehicleId("veh-100")).thenReturn(List.of());

        List<DepreciationProjection> results = adapter.findByVehicleId("veh-100");

        assertNotNull(results);
        assertTrue(results.isEmpty());
        verify(repository, times(1)).findByVehicleId("veh-100");
    }
}
