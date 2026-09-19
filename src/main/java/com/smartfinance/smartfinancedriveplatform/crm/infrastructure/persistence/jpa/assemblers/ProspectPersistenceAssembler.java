package com.smartfinance.smartfinancedriveplatform.crm.infrastructure.persistence.jpa.assemblers;

import com.smartfinance.smartfinancedriveplatform.crm.domain.model.aggregates.Prospect;
import com.smartfinance.smartfinancedriveplatform.crm.domain.model.entities.ProspectNote;
import com.smartfinance.smartfinancedriveplatform.crm.domain.model.valueobjects.ProspectId;
import com.smartfinance.smartfinancedriveplatform.crm.infrastructure.persistence.jpa.entities.ProspectNotePersistenceEntity;
import com.smartfinance.smartfinancedriveplatform.crm.infrastructure.persistence.jpa.entities.ProspectPersistenceEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class ProspectPersistenceAssembler {

    private ProspectPersistenceAssembler() {}

    public static ProspectPersistenceEntity toEntity(Prospect domain, ProspectPersistenceEntity entity) {
        if (entity == null) {
            entity = new ProspectPersistenceEntity();
        }
        entity.setId(domain.getId().value());
        entity.setDealerUserId(domain.getDealerUserId());
        entity.setBuyerUserId(domain.getBuyerUserId());
        entity.setFullName(domain.getFullName());
        entity.setEmail(domain.getEmail());
        entity.setPhone(domain.getPhone());
        entity.setInterestedVehicleId(domain.getInterestedVehicleId());
        entity.setStatus(domain.getStatus());
        entity.setSalesAgentId(domain.getSalesAgentId());

        if (domain.getNotes() != null) {
            final var finalEntity = entity;
            List<ProspectNotePersistenceEntity> noteEntities = domain.getNotes().stream().map(note -> {
                ProspectNotePersistenceEntity noteEntity = new ProspectNotePersistenceEntity();
                noteEntity.setId(note.getId());
                noteEntity.setProspect(finalEntity);
                noteEntity.setAuthorUserId(note.getAuthorUserId());
                noteEntity.setNoteText(note.getNoteText());
                return noteEntity;
            }).collect(Collectors.toList());
            entity.setNotes(noteEntities);
        }
        return entity;
    }

    public static Prospect toDomain(ProspectPersistenceEntity entity) {
        List<ProspectNote> notes = entity.getNotes() != null
                ? entity.getNotes().stream()
                .map(n -> new ProspectNote(n.getId(), entity.getId(), n.getAuthorUserId(), n.getNoteText(), n.getCreatedAt()))
                .collect(Collectors.toList())
                : new ArrayList<>();

        return new Prospect(
            new ProspectId(entity.getId()),
            entity.getDealerUserId(),
            entity.getBuyerUserId(),
            entity.getFullName(),
            entity.getEmail(),
            entity.getPhone(),
            entity.getInterestedVehicleId(),
            entity.getStatus(),
            entity.getSalesAgentId(),
            entity.getCreatedAt(),
            notes
        );
    }
}
