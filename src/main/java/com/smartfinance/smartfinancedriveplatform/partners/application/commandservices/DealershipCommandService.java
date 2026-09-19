package com.smartfinance.smartfinancedriveplatform.partners.application.commandservices;

import com.smartfinance.smartfinancedriveplatform.partners.domain.model.aggregates.Dealership;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.commands.CreateDealershipCommand;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.commands.UpdateDealershipCommand;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.valueobjects.DealershipId;

import java.util.Optional;

/**
 * Interface declaring command operations for Dealership application layer.
 */
public interface DealershipCommandService {

    Optional<Dealership> handle(CreateDealershipCommand command);

    Optional<Dealership> handle(UpdateDealershipCommand command);

    Optional<Dealership> updateLogo(DealershipId dealershipId, String logoUrl);

    Optional<Dealership> updateBanner(DealershipId dealershipId, String bannerUrl);
}
