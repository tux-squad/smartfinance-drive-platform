package com.smartfinance.smartfinancedriveplatform.partners.application.commandservices;

import com.smartfinance.smartfinancedriveplatform.partners.domain.model.aggregates.FinancialEntity;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.commands.AddRateBenchmarkCommand;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.commands.CreateFinancialEntityCommand;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.commands.DeleteFinancialEntityCommand;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.commands.UpdateFinancialEntityCommand;

import java.util.Optional;

/**
 * Interface declaring command operations for the FinancialEntity application layer.
 */
public interface FinancialEntityCommandService {

    /**
     * Handles the creation of a new financial entity.
     *
     * @param command The creation command.
     * @return An Optional containing the created financial entity.
     */
    Optional<FinancialEntity> handle(CreateFinancialEntityCommand command);

    /**
     * Handles updating an existing financial entity name.
     *
     * @param command The update command.
     * @return An Optional containing the updated financial entity if found.
     */
    Optional<FinancialEntity> handle(UpdateFinancialEntityCommand command);

    /**
     * Handles adding a new rate benchmark to a financial entity.
     *
     * @param command The add rate benchmark command.
     * @return An Optional containing the updated financial entity with the new benchmark.
     */
    Optional<FinancialEntity> handle(AddRateBenchmarkCommand command);

    /**
     * Handles deleting a financial entity.
     *
     * @param command The deletion command.
     */
    void handle(DeleteFinancialEntityCommand command);
}
