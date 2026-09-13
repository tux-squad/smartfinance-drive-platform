package com.smartfinance.smartfinancedriveplatform.catalog.infrastructure.persistence.jpa.adapters;

import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.aggregates.Vehicle;
import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.valueobjects.FinancialEntityId;
import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.valueobjects.UserId;
import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.valueobjects.VehicleId;
import com.smartfinance.smartfinancedriveplatform.catalog.infrastructure.persistence.jpa.entities.VehiclePersistenceEntity;
import com.smartfinance.smartfinancedriveplatform.catalog.infrastructure.persistence.jpa.repositories.SpringDataVehicleRepository;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.valueobjects.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for VehicleRepositoryAdapter.
 * Verifies integration mapping and delegation to the Spring Data JPA layer.
 */
@ExtendWith(MockitoExtension.class)
class VehicleRepositoryAdapterTest {

    @Mock
    private SpringDataVehicleRepository springDataVehicleRepository;

    private VehicleRepositoryAdapter vehicleRepositoryAdapter;

    @BeforeEach
    void setUp() {
        vehicleRepositoryAdapter = new VehicleRepositoryAdapter(springDataVehicleRepository);
    }

    @Test
    void testFindByIdFound() {
        UUID id = UUID.randomUUID();
        VehiclePersistenceEntity entity = new VehiclePersistenceEntity();
        entity.setId(id);
        entity.setUserId(UUID.randomUUID().toString());
        entity.setFinancialEntityId(UUID.randomUUID());
        entity.setBrand("Toyota");
        entity.setModel("Corolla");
        entity.setManufactureYear(2023);
        entity.setCondition("NEW");
        entity.setCurrency("USD");
        entity.setPrice(BigDecimal.valueOf(15000));

        when(springDataVehicleRepository.findById(id)).thenReturn(Optional.of(entity));

        Optional<Vehicle> result = vehicleRepositoryAdapter.findById(new VehicleId(id));

        assertTrue(result.isPresent());
        assertEquals("Toyota", result.get().getBrand());
    }

    @Test
    void testSaveVehicle() {
        UUID id = UUID.randomUUID();
        Vehicle vehicle = new Vehicle(
            new VehicleId(id),
            new UserId(UUID.randomUUID()),
            new FinancialEntityId(UUID.randomUUID()),
            "Toyota",
            "Corolla",
            2023,
            "NEW",
            Money.of(15000, "USD"),
            null
        );

        VehiclePersistenceEntity entity = new VehiclePersistenceEntity();
        entity.setId(id);
        entity.setUserId(vehicle.getUserId().value());
        entity.setFinancialEntityId(vehicle.getFinancialEntityId().value());
        entity.setBrand("Toyota");
        entity.setModel("Corolla");
        entity.setManufactureYear(2023);
        entity.setCondition("NEW");
        entity.setCurrency("USD");
        entity.setPrice(BigDecimal.valueOf(15000));

        when(springDataVehicleRepository.findById(id)).thenReturn(Optional.empty());
        when(springDataVehicleRepository.save(any())).thenReturn(entity);

        Vehicle result = vehicleRepositoryAdapter.save(vehicle);

        assertNotNull(result);
        assertEquals("Toyota", result.getBrand());
        verify(springDataVehicleRepository, times(1)).save(any());
    }
}
