package com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.resources;

public record CreateUpdateSalesAgentResource(
        String fullName,
        String email,
        String phone,
        Boolean active
) {
}
