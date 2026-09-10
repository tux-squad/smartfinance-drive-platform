package com.smartfinance.smartfinancedriveplatform.projections.application.queryservices;

import com.smartfinance.smartfinancedriveplatform.projections.domain.model.aggregates.DepreciationProjection;
import com.smartfinance.smartfinancedriveplatform.projections.domain.model.queries.GetAllDepreciationProjectionsQuery;
import com.smartfinance.smartfinancedriveplatform.projections.domain.model.queries.GetDepreciationProjectionByIdQuery;
import com.smartfinance.smartfinancedriveplatform.projections.domain.model.queries.GetDepreciationProjectionsByVehicleIdQuery;

import java.util.List;
import java.util.Optional;

/**
 * Query Service interface for retrieving DepreciationProjections.
 */
public interface DepreciationProjectionQueryService {
    Optional<DepreciationProjection> handle(GetDepreciationProjectionByIdQuery query);
    List<DepreciationProjection> handle(GetDepreciationProjectionsByVehicleIdQuery query);
    List<DepreciationProjection> handle(GetAllDepreciationProjectionsQuery query);
}
