package com.smartfinance.smartfinancedriveplatform.projections.domain.model.commands;

import com.smartfinance.smartfinancedriveplatform.projections.domain.model.valueobjects.ProjectionId;

/**
 * Command to delete a depreciation projection by ProjectionId.
 */
public record DeleteDepreciationProjectionCommand(ProjectionId projectionId) {}
