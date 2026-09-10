package com.smartfinance.smartfinancedriveplatform.scoring.interfaces.rest.transform;

import com.smartfinance.smartfinancedriveplatform.scoring.domain.model.commands.EvaluateCreditScoreCommand;
import com.smartfinance.smartfinancedriveplatform.scoring.interfaces.rest.resources.EvaluateCreditScoreResource;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.valueobjects.Money;

/**
 * Assembler class to transform EvaluateCreditScoreResource into EvaluateCreditScoreCommand.
 */
public final class EvaluateCreditScoreCommandFromResourceAssembler {

    private EvaluateCreditScoreCommandFromResourceAssembler() {}

    public static EvaluateCreditScoreCommand toCommandFromResource(EvaluateCreditScoreResource resource) {
        String currency = (resource.currency() != null && !resource.currency().isBlank()) ? resource.currency() : Money.DEFAULT_CURRENCY;
        Money income = resource.monthlyIncomeAmount() != null ? new Money(resource.monthlyIncomeAmount(), currency) : Money.zero(currency);
        Money installment = resource.projectedMonthlyInstallmentAmount() != null ? new Money(resource.projectedMonthlyInstallmentAmount(), currency) : Money.zero(currency);

        return new EvaluateCreditScoreCommand(
                resource.profileId(),
                resource.simulationId(),
                income,
                installment
        );
    }
}
