package com.smartfinance.smartfinancedriveplatform.catalog.infrastructure.persistence.jpa.adapters;

import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.aggregates.Vehicle;
import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.queries.GetAllVehiclesQuery;
import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.valueobjects.UserId;
import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.valueobjects.VehicleId;
import com.smartfinance.smartfinancedriveplatform.catalog.domain.repositories.VehicleRepository;
import com.smartfinance.smartfinancedriveplatform.catalog.infrastructure.persistence.jpa.assemblers.VehiclePersistenceAssembler;
import com.smartfinance.smartfinancedriveplatform.catalog.infrastructure.persistence.jpa.entities.VehiclePersistenceEntity;
import com.smartfinance.smartfinancedriveplatform.catalog.infrastructure.persistence.jpa.repositories.SpringDataVehicleRepository;
import com.smartfinance.smartfinancedriveplatform.catalog.infrastructure.persistence.jpa.specifications.VehicleSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adapter implementing the VehicleRepository interface from the domain layer
 * by delegating to Spring Data JPA and assembling results.
 */
@Component
public class VehicleRepositoryAdapter implements VehicleRepository {

    private final SpringDataVehicleRepository springDataVehicleRepository;

    public VehicleRepositoryAdapter(SpringDataVehicleRepository springDataVehicleRepository) {
        this.springDataVehicleRepository = springDataVehicleRepository;
    }

    @Override
    public Vehicle save(Vehicle vehicle) {
        VehiclePersistenceEntity existingEntity = springDataVehicleRepository.findById(vehicle.getId().value()).orElse(null);
        VehiclePersistenceEntity entityToSave = VehiclePersistenceAssembler.toEntity(vehicle, existingEntity);
        VehiclePersistenceEntity savedEntity = springDataVehicleRepository.save(entityToSave);
        return VehiclePersistenceAssembler.toDomain(savedEntity);
    }

    @Override
    public Optional<Vehicle> findById(VehicleId id) {
        return springDataVehicleRepository.findById(id.value())
                .map(VehiclePersistenceAssembler::toDomain);
    }

    @Override
    public List<Vehicle> findAllByUserId(UserId userId) {
        return springDataVehicleRepository.findAllByUserId(userId.value()).stream()
                .map(VehiclePersistenceAssembler::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Page<Vehicle> findAll(GetAllVehiclesQuery query, Pageable pageable) {
        var spec = VehicleSpecification.withFilter(query);
        return springDataVehicleRepository.findAll(spec, pageable)
                .map(VehiclePersistenceAssembler::toDomain);
    }

    @Override
    public boolean existsById(VehicleId id) {
        return springDataVehicleRepository.existsById(id.value());
    }

    @Override
    public void deleteById(VehicleId id) {
        springDataVehicleRepository.deleteById(id.value());
    }
}
