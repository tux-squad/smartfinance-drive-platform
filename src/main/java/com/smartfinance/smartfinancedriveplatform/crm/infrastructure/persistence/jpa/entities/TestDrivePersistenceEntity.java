package com.smartfinance.smartfinancedriveplatform.crm.infrastructure.persistence.jpa.entities;

import com.smartfinance.smartfinancedriveplatform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "test_drives", indexes = {
        @Index(name = "idx_test_drive_buyer", columnList = "buyer_user_id"),
        @Index(name = "idx_test_drive_dealership", columnList = "dealership_id")
})
@Getter
@Setter
@NoArgsConstructor
public class TestDrivePersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Column(name = "buyer_user_id", nullable = false)
    private String buyerUserId;

    @Column(name = "vehicle_id", nullable = false)
    private UUID vehicleId;

    @Column(name = "dealership_id", nullable = false)
    private UUID dealershipId;

    @Column(name = "scheduled_date_time", nullable = false)
    private LocalDateTime scheduledDateTime;

    @Column(name = "status", nullable = false, length = 30)
    private String status = "SCHEDULED";

    @Column(name = "notes", length = 1000)
    private String notes;
}
