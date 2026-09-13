package com.smartfinance.smartfinancedriveplatform.projections.infrastructure.persistence.jpa.adapters;

import com.smartfinance.smartfinancedriveplatform.projections.domain.model.aggregates.DepreciationProjection;
import com.smartfinance.smartfinancedriveplatform.projections.domain.model.valueobjects.ProjectionId;
import com.smartfinance.smartfinancedriveplatform.projections.domain.repositories.DepreciationProjectionRepository;
import com.smartfinance.smartfinancedriveplatform.projections.infrastructure.persistence.jpa.assemblers.DepreciationProjectionPersistenceAssembler;
import com.smartfinance.smartfinancedriveplatform.projections.infrastructure.persistence.jpa.entities.DepreciationProjectionPersistenceEntity;
import com.smartfinance.smartfinancedriveplatform.projections.infrastructure.persistence.jpa.repositories.SpringDataDepreciationProjectionRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adapter implementing the DepreciationProjectionRepository domain interface.
 */
@Component
public class DepreciationProjectionRepositoryAdapter implements DepreciationProjectionRepository {

    private final SpringDataDepreciationProjectionRepository repository;

    public DepreciationProjectionRepositoryAdapter(SpringDataDepreciationProjectionRepository repository) {
        this.repository = repository;
    }

    @Override
    public DepreciationProjection save(DepreciationProjection projection) {
        DepreciationProjectionPersistenceEntity existingEntity = repository.findById(projection.getId().value()).orElse(null);
        DepreciationProjectionPersistenceEntity entityToSave = DepreciationProjectionPersistenceAssembler.toEntity(projection, existingEntity);
        DepreciationProjectionPersistenceEntity savedEntity = repository.save(entityToSave);
        return DepreciationProjectionPersistenceAssembler.toDomain(savedEntity);
    }

    @Override
    public Optional<DepreciationProjection> findById(ProjectionId projectionId) {
        return repository.findById(projectionId.value())
                .map(DepreciationProjectionPersistenceAssembler::toDomain);
    }

    @Override
    public List<DepreciationProjection> findByVehicleId(String vehicleId) {
        return repository.findByVehicleId(vehicleId).stream()
                .map(DepreciationProjectionPersistenceAssembler::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<DepreciationProjection> findAll() {
        return repository.findAll().stream()
                .map(DepreciationProjectionPersistenceAssembler::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public org.springframework.data.domain.Page<DepreciationProjection> findAll(org.springframework.data.domain.Pageable pageable) {
        return repository.findAll(pageable)
                .map(DepreciationProjectionPersistenceAssembler::toDomain);
    }

    @Override
    public org.springframework.data.domain.Page<DepreciationProjection> findByVehicleId(String vehicleId, org.springframework.data.domain.Pageable pageable) {
        return repository.findByVehicleId(vehicleId, pageable)
                .map(DepreciationProjectionPersistenceAssembler::toDomain);
    }

    @Override
    public void deleteById(ProjectionId projectionId) {
        repository.deleteById(projectionId.value());
    }

    @Override
    public boolean existsById(ProjectionId projectionId) {
        return repository.existsById(projectionId.value());
    }
}
