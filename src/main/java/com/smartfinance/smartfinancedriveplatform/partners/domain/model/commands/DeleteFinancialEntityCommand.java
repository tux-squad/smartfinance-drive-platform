package com.smartfinance.smartfinancedriveplatform.partners.domain.model.commands;

import com.smartfinance.smartfinancedriveplatform.partners.domain.model.valueobjects.FinancialEntityId;

/**
 * Command to request the deletion of a financial entity.
 */
public record DeleteFinancialEntityCommand(FinancialEntityId financialEntityId) {}
