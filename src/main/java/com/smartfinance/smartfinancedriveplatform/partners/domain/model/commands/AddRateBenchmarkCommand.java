package com.smartfinance.smartfinancedriveplatform.partners.domain.model.commands;

import com.smartfinance.smartfinancedriveplatform.partners.domain.model.valueobjects.FinancialEntityId;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.valueobjects.Percent;

import java.time.LocalDate;

/**
 * Command to request adding a new rate benchmark to a financial entity.
 */
public record AddRateBenchmarkCommand(
    FinancialEntityId financialEntityId,
    String rateType,
    Percent annualRate,
    String currency,
    String sourceLabel,
    String sourceUrl,
    LocalDate effectiveFrom
) {}
