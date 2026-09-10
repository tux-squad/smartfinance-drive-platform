package com.smartfinance.smartfinancedriveplatform.partners.domain.model.queries;

import com.smartfinance.smartfinancedriveplatform.partners.domain.model.valueobjects.FinancialEntityId;

/**
 * Query to retrieve a financial entity by its unique ID.
 */
public record GetFinancialEntityByIdQuery(FinancialEntityId financialEntityId) {}
