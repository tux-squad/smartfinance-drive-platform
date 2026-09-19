package com.smartfinance.smartfinancedriveplatform.crm.domain.model.commands;

import java.time.LocalDateTime;
import java.util.UUID;

public record ScheduleTestDriveCommand(
    String buyerUserId,
    UUID vehicleId,
    UUID dealershipId,
    LocalDateTime scheduledDateTime,
    String notes
) {}
