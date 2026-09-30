package com.smartfinance.smartfinancedriveplatform.partners.domain.model.commands;

import com.smartfinance.smartfinancedriveplatform.partners.domain.model.valueobjects.FinancialEntityId;

/**
 * Command to request updating an existing financial entity name.
 */
public record UpdateFinancialEntityCommand(
    FinancialEntityId financialEntityId,
    String userId,
    String name,
    String logoUrl,
    String bannerUrl
) {
    public UpdateFinancialEntityCommand(FinancialEntityId financialEntityId, String name, String logoUrl, String bannerUrl) {
        this(financialEntityId, null, name, logoUrl, bannerUrl);
    }

    public UpdateFinancialEntityCommand(FinancialEntityId financialEntityId, String name) {
        this(financialEntityId, null, name, null, null);
    }
}
