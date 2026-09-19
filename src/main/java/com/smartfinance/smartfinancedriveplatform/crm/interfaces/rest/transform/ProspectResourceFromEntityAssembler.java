package com.smartfinance.smartfinancedriveplatform.crm.interfaces.rest.transform;

import com.smartfinance.smartfinancedriveplatform.crm.domain.model.aggregates.Prospect;
import com.smartfinance.smartfinancedriveplatform.crm.interfaces.rest.resources.ProspectNoteResource;
import com.smartfinance.smartfinancedriveplatform.crm.interfaces.rest.resources.ProspectResource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class ProspectResourceFromEntityAssembler {

    private ProspectResourceFromEntityAssembler() {}

    public static ProspectResource toResourceFromEntity(Prospect domain) {
        List<ProspectNoteResource> noteResources = domain.getNotes() != null
                ? domain.getNotes().stream()
                .map(n -> new ProspectNoteResource(n.getId(), n.getProspectId(), n.getAuthorUserId(), n.getNoteText(), n.getCreatedAt()))
                .collect(Collectors.toList())
                : new ArrayList<>();

        return new ProspectResource(
            domain.getId().value(),
            domain.getDealerUserId(),
            domain.getBuyerUserId(),
            domain.getFullName(),
            domain.getEmail(),
            domain.getPhone(),
            domain.getInterestedVehicleId(),
            domain.getStatus(),
            domain.getSalesAgentId(),
            domain.getCreatedAt(),
            noteResources
        );
    }
}
