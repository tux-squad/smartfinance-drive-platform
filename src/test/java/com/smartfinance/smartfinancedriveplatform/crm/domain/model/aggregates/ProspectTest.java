package com.smartfinance.smartfinancedriveplatform.crm.domain.model.aggregates;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Prospect Aggregate Root Unit Tests")
class ProspectTest {

    @Test
    @DisplayName("Should create prospect and add timeline notes")
    void shouldCreateProspectAndAddNote() {
        Prospect prospect = new Prospect(
                "dealer-1",
                "buyer-1",
                "Juan Perez",
                "juan@example.com",
                "987654321",
                UUID.randomUUID(),
                "agent-1"
        );

        assertThat(prospect.getId()).isNotNull();
        assertThat(prospect.getStatus()).isEqualTo("NEW");
        assertThat(prospect.getNotes()).isEmpty();

        prospect.addNote("dealer-1", "Cliente interesado en financiamiento BCP a 36 meses");
        assertThat(prospect.getNotes()).hasSize(1);
        assertThat(prospect.getNotes().get(0).getNoteText()).contains("BCP a 36 meses");
    }
}
