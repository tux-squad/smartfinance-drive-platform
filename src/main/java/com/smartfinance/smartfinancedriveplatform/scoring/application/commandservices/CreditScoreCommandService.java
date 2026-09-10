package com.smartfinance.smartfinancedriveplatform.scoring.application.commandservices;

import com.smartfinance.smartfinancedriveplatform.scoring.domain.model.aggregates.CreditScore;
import com.smartfinance.smartfinancedriveplatform.scoring.domain.model.commands.DeleteCreditScoreCommand;
import com.smartfinance.smartfinancedriveplatform.scoring.domain.model.commands.EvaluateCreditScoreCommand;

import java.util.Optional;

/**
 * Command Service interface for CreditScore mutations.
 */
public interface CreditScoreCommandService {
    Optional<CreditScore> handle(EvaluateCreditScoreCommand command);
    void handle(DeleteCreditScoreCommand command);
}
