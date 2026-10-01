package com.smartfinance.smartfinancedriveplatform.partners.application.commandservices;

import com.smartfinance.smartfinancedriveplatform.partners.domain.model.aggregates.FinancialEntity;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.commands.AddRateBenchmarkCommand;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.commands.CreateFinancialEntityCommand;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.commands.DeleteFinancialEntityCommand;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.commands.LinkFinancialEntityToUserCommand;
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
     * Handles linking or creating a financial entity for a user with verified RUC.
     *
     * @param command The link financial entity command.
     * @return The linked or created financial entity.
     */
    FinancialEntity handle(LinkFinancialEntityToUserCommand command);

    /**
     * Updates the logo URL of a financial entity.
     *
     * @param financialEntityId The financial entity ID.
     * @param logoUrl           The uploaded logo URL.
     * @return An Optional containing the updated financial entity if found.
     */
    Optional<FinancialEntity> updateLogo(com.smartfinance.smartfinancedriveplatform.partners.domain.model.valueobjects.FinancialEntityId financialEntityId, String logoUrl);

    /**
     * Updates the banner URL of a financial entity.
     *
     * @param financialEntityId The financial entity ID.
     * @param bannerUrl         The uploaded banner URL.
     * @return An Optional containing the updated financial entity if found.
     */
    Optional<FinancialEntity> updateBanner(com.smartfinance.smartfinancedriveplatform.partners.domain.model.valueobjects.FinancialEntityId financialEntityId, String bannerUrl);

    /**
     * Handles deleting a financial entity.
     *
     * @param command The deletion command.
     */
    void handle(DeleteFinancialEntityCommand command);
}
