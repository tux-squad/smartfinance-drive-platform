package com.smartfinance.smartfinancedriveplatform.crm.domain.model.queries;

import com.smartfinance.smartfinancedriveplatform.crm.domain.model.valueobjects.TestDriveId;

public record GetTestDriveByIdQuery(TestDriveId testDriveId) {
    public GetTestDriveByIdQuery {
        if (testDriveId == null) {
            throw new IllegalArgumentException("testDriveId cannot be null");
        }
    }
}
