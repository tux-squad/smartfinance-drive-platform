package com.smartfinance.smartfinancedriveplatform.crm.interfaces.rest.resources;

import java.time.Instant;
import java.util.UUID;

public record ProspectNoteResource(
    UUID id,
    UUID prospectId,
    String authorUserId,
    String noteText,
    Instant createdAt
) {}
