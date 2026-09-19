package com.smartfinance.smartfinancedriveplatform.crm.domain.model.entities;

import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class ProspectNote {

    private final UUID id;
    private final UUID prospectId;
    private final String authorUserId;
    private final String noteText;
    private final Instant createdAt;

    public ProspectNote(UUID id, UUID prospectId, String authorUserId, String noteText, Instant createdAt) {
        if (id == null) throw new DomainValidationException("crm.error.noteId.required");
        if (prospectId == null) throw new DomainValidationException("crm.error.prospectId.required");
        if (authorUserId == null || authorUserId.isBlank()) throw new DomainValidationException("crm.error.authorUserId.required");
        if (noteText == null || noteText.isBlank()) throw new DomainValidationException("crm.error.noteText.required");
        this.id = id;
        this.prospectId = prospectId;
        this.authorUserId = authorUserId.trim();
        this.noteText = noteText.trim();
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public ProspectNote(UUID prospectId, String authorUserId, String noteText) {
        this(UUID.randomUUID(), prospectId, authorUserId, noteText, Instant.now());
    }
}
