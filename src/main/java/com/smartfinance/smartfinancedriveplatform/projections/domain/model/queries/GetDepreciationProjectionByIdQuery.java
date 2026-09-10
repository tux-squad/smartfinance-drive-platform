package com.smartfinance.smartfinancedriveplatform.projections.domain.model.queries;

import com.smartfinance.smartfinancedriveplatform.projections.domain.model.valueobjects.ProjectionId;

/**
 * Query to retrieve a depreciation projection by ProjectionId.
 */
public record GetDepreciationProjectionByIdQuery(ProjectionId projectionId) {}
