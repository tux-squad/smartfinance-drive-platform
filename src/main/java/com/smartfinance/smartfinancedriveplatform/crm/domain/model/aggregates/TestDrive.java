package com.smartfinance.smartfinancedriveplatform.crm.domain.model.aggregates;

import com.smartfinance.smartfinancedriveplatform.crm.domain.model.valueobjects.TestDriveId;
import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * TestDrive Aggregate Root.
 * Statuses: SCHEDULED, COMPLETED, CANCELLED.
 */
@Getter
public class TestDrive extends AbstractDomainAggregateRoot<TestDrive> {

    private final TestDriveId id;
    private String buyerUserId;
    private UUID vehicleId;
    private UUID dealershipId;
    private LocalDateTime scheduledDateTime;
    private String status;
    private String notes;

    public TestDrive(TestDriveId id, String buyerUserId, UUID vehicleId, UUID dealershipId, 
                     LocalDateTime scheduledDateTime, String status, String notes) {
        this.id = id;
        setBuyerUserId(buyerUserId);
        setVehicleId(vehicleId);
        setDealershipId(dealershipId);
        setScheduledDateTime(scheduledDateTime);
        setStatus(status);
        this.notes = notes;
    }

    public TestDrive(String buyerUserId, UUID vehicleId, UUID dealershipId, LocalDateTime scheduledDateTime, String notes) {
        this(new TestDriveId(UUID.randomUUID()), buyerUserId, vehicleId, dealershipId, scheduledDateTime, "SCHEDULED", notes);
    }

    public void setBuyerUserId(String buyerUserId) {
        if (buyerUserId == null || buyerUserId.isBlank()) {
            throw new DomainValidationException("crm.error.testDrive.buyerUserId.required");
        }
        this.buyerUserId = buyerUserId.trim();
    }

    public void setVehicleId(UUID vehicleId) {
        if (vehicleId == null) {
            throw new DomainValidationException("crm.error.testDrive.vehicleId.required");
        }
        this.vehicleId = vehicleId;
    }

    public void setDealershipId(UUID dealershipId) {
        if (dealershipId == null) {
            throw new DomainValidationException("crm.error.testDrive.dealershipId.required");
        }
        this.dealershipId = dealershipId;
    }

    public void setScheduledDateTime(LocalDateTime scheduledDateTime) {
        if (scheduledDateTime == null) {
            throw new DomainValidationException("crm.error.testDrive.scheduledDateTime.required");
        }
        this.scheduledDateTime = scheduledDateTime;
    }

    public void setStatus(String status) {
        if (status == null || status.isBlank()) {
            this.status = "SCHEDULED";
            return;
        }
        String normalized = status.trim().toUpperCase();
        if (!normalized.equals("SCHEDULED") && !normalized.equals("COMPLETED") && !normalized.equals("CANCELLED")) {
            throw new DomainValidationException("crm.error.testDriveStatus.invalid");
        }
        this.status = normalized;
    }
}
