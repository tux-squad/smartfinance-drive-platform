package com.smartfinance.smartfinancedriveplatform.financing.domain.model.commands;

import com.smartfinance.smartfinancedriveplatform.financing.domain.model.valueobjects.CreditApplicationId;

/**
 * Command to request updating status of a credit application.
 */
public record UpdateCreditApplicationStatusCommand(
    CreditApplicationId creditApplicationId,
    String status,
    String notes
) {}
