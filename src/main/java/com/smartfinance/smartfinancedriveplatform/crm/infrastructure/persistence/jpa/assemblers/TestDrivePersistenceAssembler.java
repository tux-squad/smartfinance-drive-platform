package com.smartfinance.smartfinancedriveplatform.crm.infrastructure.persistence.jpa.assemblers;

import com.smartfinance.smartfinancedriveplatform.crm.domain.model.aggregates.TestDrive;
import com.smartfinance.smartfinancedriveplatform.crm.domain.model.valueobjects.TestDriveId;
import com.smartfinance.smartfinancedriveplatform.crm.infrastructure.persistence.jpa.entities.TestDrivePersistenceEntity;

public final class TestDrivePersistenceAssembler {

    private TestDrivePersistenceAssembler() {}

    public static TestDrivePersistenceEntity toEntity(TestDrive domain, TestDrivePersistenceEntity entity) {
        if (entity == null) {
            entity = new TestDrivePersistenceEntity();
        }
        entity.setId(domain.getId().value());
        entity.setBuyerUserId(domain.getBuyerUserId());
        entity.setVehicleId(domain.getVehicleId());
        entity.setDealershipId(domain.getDealershipId());
        entity.setScheduledDateTime(domain.getScheduledDateTime());
        entity.setStatus(domain.getStatus());
        entity.setNotes(domain.getNotes());
        return entity;
    }

    public static TestDrive toDomain(TestDrivePersistenceEntity entity) {
        return new TestDrive(
            new TestDriveId(entity.getId()),
            entity.getBuyerUserId(),
            entity.getVehicleId(),
            entity.getDealershipId(),
            entity.getScheduledDateTime(),
            entity.getStatus(),
            entity.getNotes()
        );
    }
}
