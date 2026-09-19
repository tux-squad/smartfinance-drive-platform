package com.smartfinance.smartfinancedriveplatform.crm.interfaces.rest.resources;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ProspectResource(
    UUID id,
    String dealerUserId,
    String buyerUserId,
    String fullName,
    String email,
    String phone,
    UUID interestedVehicleId,
    String status,
    String salesAgentId,
    Instant createdAt,
    List<ProspectNoteResource> notes
) {}
