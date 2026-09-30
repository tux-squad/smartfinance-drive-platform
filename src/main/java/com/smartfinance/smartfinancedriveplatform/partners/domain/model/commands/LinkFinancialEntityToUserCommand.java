package com.smartfinance.smartfinancedriveplatform.partners.domain.model.commands;

/**
 * Command to link a financial entity to a user upon verified SUNAT financial institution role request.
 *
 * @param userId    The authenticated user ID to link.
 * @param ruc       The verified 11-digit RUC.
 * @param legalName The official company/bank legal name from SUNAT.
 */
public record LinkFinancialEntityToUserCommand(
    String userId,
    String ruc,
    String legalName
) {
}
