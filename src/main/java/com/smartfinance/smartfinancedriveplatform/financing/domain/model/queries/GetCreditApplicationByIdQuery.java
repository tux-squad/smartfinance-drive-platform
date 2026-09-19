package com.smartfinance.smartfinancedriveplatform.financing.domain.model.queries;

import com.smartfinance.smartfinancedriveplatform.financing.domain.model.valueobjects.CreditApplicationId;

/**
 * Query to retrieve credit application by unique ID.
 */
public record GetCreditApplicationByIdQuery(CreditApplicationId creditApplicationId) {}
