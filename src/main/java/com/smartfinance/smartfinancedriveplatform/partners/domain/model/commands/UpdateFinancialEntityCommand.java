package com.smartfinance.smartfinancedriveplatform.partners.domain.model.commands;

import com.smartfinance.smartfinancedriveplatform.partners.domain.model.valueobjects.FinancialEntityId;

/**
 * Command to request updating an existing financial entity name.
 */
public record UpdateFinancialEntityCommand(
    FinancialEntityId financialEntityId,
    String name
) {}
