package com.smartfinance.smartfinancedriveplatform.crm.domain.model.commands;

import com.smartfinance.smartfinancedriveplatform.crm.domain.model.valueobjects.TestDriveId;

public record UpdateTestDriveStatusCommand(TestDriveId testDriveId, String status) {
    public UpdateTestDriveStatusCommand {
        if (testDriveId == null) {
            throw new IllegalArgumentException("testDriveId cannot be null");
        }
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("status cannot be null or blank");
        }
    }
}
