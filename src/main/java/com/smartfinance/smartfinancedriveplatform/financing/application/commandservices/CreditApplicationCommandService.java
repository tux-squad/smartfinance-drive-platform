package com.smartfinance.smartfinancedriveplatform.financing.application.commandservices;

import com.smartfinance.smartfinancedriveplatform.financing.domain.model.aggregates.CreditApplication;
import com.smartfinance.smartfinancedriveplatform.financing.domain.model.commands.CreateCreditApplicationCommand;
import com.smartfinance.smartfinancedriveplatform.financing.domain.model.commands.UpdateCreditApplicationStatusCommand;

import java.util.Optional;

/**
 * Interface declaring command operations for CreditApplication.
 */
public interface CreditApplicationCommandService {

    Optional<CreditApplication> handle(CreateCreditApplicationCommand command);

    Optional<CreditApplication> handle(UpdateCreditApplicationStatusCommand command);
}
