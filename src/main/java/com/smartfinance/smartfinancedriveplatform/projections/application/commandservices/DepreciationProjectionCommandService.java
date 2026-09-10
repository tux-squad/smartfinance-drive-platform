package com.smartfinance.smartfinancedriveplatform.projections.application.commandservices;

import com.smartfinance.smartfinancedriveplatform.projections.domain.model.aggregates.DepreciationProjection;
import com.smartfinance.smartfinancedriveplatform.projections.domain.model.commands.CalculateDepreciationProjectionCommand;
import com.smartfinance.smartfinancedriveplatform.projections.domain.model.commands.DeleteDepreciationProjectionCommand;

import java.util.Optional;

/**
 * Command Service interface for DepreciationProjection mutations.
 */
public interface DepreciationProjectionCommandService {
    Optional<DepreciationProjection> handle(CalculateDepreciationProjectionCommand command);
    void handle(DeleteDepreciationProjectionCommand command);
}
