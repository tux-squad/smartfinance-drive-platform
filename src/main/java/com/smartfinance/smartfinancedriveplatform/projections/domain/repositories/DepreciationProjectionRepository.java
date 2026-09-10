package com.smartfinance.smartfinancedriveplatform.projections.domain.repositories;

import com.smartfinance.smartfinancedriveplatform.projections.domain.model.aggregates.DepreciationProjection;
import com.smartfinance.smartfinancedriveplatform.projections.domain.model.valueobjects.ProjectionId;

import java.util.List;
import java.util.Optional;

/**
 * Domain Repository interface for DepreciationProjection aggregate root.
 */
public interface DepreciationProjectionRepository {
    DepreciationProjection save(DepreciationProjection projection);
    Optional<DepreciationProjection> findById(ProjectionId projectionId);
    List<DepreciationProjection> findByVehicleId(String vehicleId);
    List<DepreciationProjection> findAll();
    void deleteById(ProjectionId projectionId);
    boolean existsById(ProjectionId projectionId);
}
