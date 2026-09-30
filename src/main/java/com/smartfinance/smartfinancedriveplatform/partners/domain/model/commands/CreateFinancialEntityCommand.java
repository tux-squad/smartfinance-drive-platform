package com.smartfinance.smartfinancedriveplatform.partners.domain.model.commands;

/**
 * Command to request the registration of a new financial entity.
 */
public record CreateFinancialEntityCommand(
    String userId,
    String ruc,
    String name,
    String logoUrl,
    String bannerUrl
) {
    public CreateFinancialEntityCommand(String userId, String name, String logoUrl, String bannerUrl) {
        this(userId, null, name, logoUrl, bannerUrl);
    }

    public CreateFinancialEntityCommand(String name, String logoUrl, String bannerUrl) {
        this(null, null, name, logoUrl, bannerUrl);
    }

    public CreateFinancialEntityCommand(String name) {
        this(null, null, name, null, null);
    }
}
