package com.smartfinance.smartfinancedriveplatform.projections.domain.repositories;

import com.smartfinance.smartfinancedriveplatform.projections.domain.model.aggregates.DepreciationProjection;
import com.smartfinance.smartfinancedriveplatform.projections.domain.model.valueobjects.ProjectionId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Domain Repository interface for DepreciationProjection aggregate root.
 */
public interface DepreciationProjectionRepository {
    DepreciationProjection save(DepreciationProjection projection);
    Optional<DepreciationProjection> findById(ProjectionId projectionId);
    List<DepreciationProjection> findByVehicleId(String vehicleId);
    Page<DepreciationProjection> findByVehicleId(String vehicleId, Pageable pageable);
    List<DepreciationProjection> findAll();
    Page<DepreciationProjection> findAll(Pageable pageable);
    void deleteById(ProjectionId projectionId);
    boolean existsById(ProjectionId projectionId);
}
