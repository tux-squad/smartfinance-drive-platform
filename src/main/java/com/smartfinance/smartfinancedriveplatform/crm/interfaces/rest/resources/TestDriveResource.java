package com.smartfinance.smartfinancedriveplatform.crm.interfaces.rest.resources;

import java.time.LocalDateTime;
import java.util.UUID;

public record TestDriveResource(
    UUID id,
    String buyerUserId,
    UUID vehicleId,
    UUID dealershipId,
    LocalDateTime scheduledDateTime,
    String status,
    String notes
) {}
