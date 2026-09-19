package com.smartfinance.smartfinancedriveplatform.crm.domain.model.queries;

public record GetTestDrivesForUserQuery(String userId) {
    public GetTestDrivesForUserQuery {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId cannot be null or blank");
        }
    }
}
