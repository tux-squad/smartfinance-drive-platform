package com.smartfinance.smartfinancedriveplatform.partners.domain.model.valueobjects;

/**
 * Value Object representing the corporate entity type for B2B verification.
 */
public enum CorporateEntityType {
    FINANCIAL_INSTITUTION("ROLE_FINANCIAL_INSTITUTION"),
    DEALERSHIP("ROLE_DEALER"),
    UNKNOWN("ROLE_USER");

    private final String targetRole;

    CorporateEntityType(String targetRole) {
        this.targetRole = targetRole;
    }

    public String getTargetRole() {
        return targetRole;
    }
}
