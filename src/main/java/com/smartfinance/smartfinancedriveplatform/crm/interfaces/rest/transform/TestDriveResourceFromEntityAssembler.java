package com.smartfinance.smartfinancedriveplatform.crm.interfaces.rest.transform;

import com.smartfinance.smartfinancedriveplatform.crm.domain.model.aggregates.TestDrive;
import com.smartfinance.smartfinancedriveplatform.crm.interfaces.rest.resources.TestDriveResource;

public final class TestDriveResourceFromEntityAssembler {

    private TestDriveResourceFromEntityAssembler() {}

    public static TestDriveResource toResourceFromEntity(TestDrive domain) {
        return new TestDriveResource(
            domain.getId().value(),
            domain.getBuyerUserId(),
            domain.getVehicleId(),
            domain.getDealershipId(),
            domain.getScheduledDateTime(),
            domain.getStatus(),
            domain.getNotes()
        );
    }
}
