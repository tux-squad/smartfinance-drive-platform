package com.smartfinance.smartfinancedriveplatform.partners.domain.model.commands;

/**
 * Command to request the registration of a new financial entity.
 */
public record CreateFinancialEntityCommand(
    String userId,
    String name,
    String logoUrl,
    String bannerUrl
) {
    public CreateFinancialEntityCommand(String name, String logoUrl, String bannerUrl) {
        this(null, name, logoUrl, bannerUrl);
    }

    public CreateFinancialEntityCommand(String name) {
        this(null, name, null, null);
    }
}
