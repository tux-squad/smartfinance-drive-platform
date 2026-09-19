package com.smartfinance.smartfinancedriveplatform.crm.domain.model.entities;

import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ProspectNoteTest {

    @Test
    void shouldCreateProspectNoteWithValidFields() {
        UUID prospectId = UUID.randomUUID();
        ProspectNote note = new ProspectNote(prospectId, "agent-1", "Client requested financial simulation.");

        assertNotNull(note.getId());
        assertEquals(prospectId, note.getProspectId());
        assertEquals("agent-1", note.getAuthorUserId());
        assertEquals("Client requested financial simulation.", note.getNoteText());
        assertNotNull(note.getCreatedAt());
    }

    @Test
    void shouldThrowExceptionWhenProspectIdIsNull() {
        assertThrows(DomainValidationException.class, () -> 
            new ProspectNote(null, "agent-1", "Valid text")
        );
    }

    @Test
    void shouldThrowExceptionWhenAuthorUserIdIsBlank() {
        assertThrows(DomainValidationException.class, () -> 
            new ProspectNote(UUID.randomUUID(), "   ", "Valid text")
        );
    }

    @Test
    void shouldThrowExceptionWhenNoteTextIsBlank() {
        assertThrows(DomainValidationException.class, () -> 
            new ProspectNote(UUID.randomUUID(), "agent-1", "")
        );
    }
}
