package com.smartfinance.smartfinancedriveplatform.projections.application.internal.queryservices;

import com.smartfinance.smartfinancedriveplatform.projections.application.queryservices.DepreciationProjectionQueryService;
import com.smartfinance.smartfinancedriveplatform.projections.domain.model.aggregates.DepreciationProjection;
import com.smartfinance.smartfinancedriveplatform.projections.domain.model.queries.GetAllDepreciationProjectionsQuery;
import com.smartfinance.smartfinancedriveplatform.projections.domain.model.queries.GetDepreciationProjectionByIdQuery;
import com.smartfinance.smartfinancedriveplatform.projections.domain.model.queries.GetDepreciationProjectionsByVehicleIdQuery;
import com.smartfinance.smartfinancedriveplatform.projections.domain.repositories.DepreciationProjectionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of DepreciationProjectionQueryService application service.
 */
@Service
public class DepreciationProjectionQueryServiceImpl implements DepreciationProjectionQueryService {

    private final DepreciationProjectionRepository repository;

    public DepreciationProjectionQueryServiceImpl(DepreciationProjectionRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DepreciationProjection> handle(GetDepreciationProjectionByIdQuery query) {
        return repository.findById(query.projectionId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepreciationProjection> handle(GetDepreciationProjectionsByVehicleIdQuery query) {
        return repository.findByVehicleId(query.vehicleId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepreciationProjection> handle(GetAllDepreciationProjectionsQuery query) {
        return repository.findAll();
    }
}
