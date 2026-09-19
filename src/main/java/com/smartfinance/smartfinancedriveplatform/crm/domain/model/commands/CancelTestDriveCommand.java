package com.smartfinance.smartfinancedriveplatform.crm.domain.model.commands;

import com.smartfinance.smartfinancedriveplatform.crm.domain.model.valueobjects.TestDriveId;

public record CancelTestDriveCommand(TestDriveId testDriveId) {
    public CancelTestDriveCommand {
        if (testDriveId == null) {
            throw new IllegalArgumentException("testDriveId cannot be null");
        }
    }
}
