package com.smartfinance.smartfinancedriveplatform.crm.interfaces.rest.resources;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

public record ScheduleTestDriveResource(
    @NotNull(message = "Vehicle ID is required")
    UUID vehicleId,

    @NotNull(message = "Dealership ID is required")
    UUID dealershipId,

    @NotNull(message = "Scheduled date time is required")
    LocalDateTime scheduledDateTime,

    String notes
) {}
