package com.smartfinance.smartfinancedriveplatform.crm.infrastructure.persistence.jpa.entities;

import com.smartfinance.smartfinancedriveplatform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "prospect_notes")
@Getter
@Setter
@NoArgsConstructor
public class ProspectNotePersistenceEntity extends AuditableAbstractPersistenceEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prospect_id", nullable = false)
    private ProspectPersistenceEntity prospect;

    @Column(name = "author_user_id", nullable = false)
    private String authorUserId;

    @Column(name = "note_text", nullable = false, length = 2000)
    private String noteText;
}
