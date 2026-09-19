package com.smartfinance.smartfinancedriveplatform.crm.domain.model.aggregates;

import com.smartfinance.smartfinancedriveplatform.crm.domain.model.entities.ProspectNote;
import com.smartfinance.smartfinancedriveplatform.crm.domain.model.valueobjects.ProspectId;
import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Prospect Aggregate Root for Dealer CRM.
 * Statuses: NEW, CONTACTED, TEST_DRIVE_SCHEDULED, NEGOTIATING, CLOSED_WON, CLOSED_LOST.
 */
@Getter
public class Prospect extends AbstractDomainAggregateRoot<Prospect> {

    private final ProspectId id;
    private String dealerUserId;
    private String buyerUserId;
    private String fullName;
    private String email;
    private String phone;
    private UUID interestedVehicleId;
    private String status;
    private String salesAgentId;
    private Instant createdAt;
    private List<ProspectNote> notes = new ArrayList<>();

    public Prospect(ProspectId id, String dealerUserId, String buyerUserId, String fullName, 
                    String email, String phone, UUID interestedVehicleId, String status, 
                    String salesAgentId, Instant createdAt, List<ProspectNote> notes) {
        this.id = id;
        setDealerUserId(dealerUserId);
        this.buyerUserId = buyerUserId;
        setFullName(fullName);
        this.email = email;
        this.phone = phone;
        this.interestedVehicleId = interestedVehicleId;
        setStatus(status);
        this.salesAgentId = salesAgentId;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        if (notes != null) {
            this.notes = new ArrayList<>(notes);
        }
    }

    public Prospect(String dealerUserId, String buyerUserId, String fullName, String email, 
                    String phone, UUID interestedVehicleId, String salesAgentId) {
        this(new ProspectId(UUID.randomUUID()), dealerUserId, buyerUserId, fullName, email, phone, interestedVehicleId, "NEW", salesAgentId, Instant.now(), new ArrayList<>());
    }

    public void setDealerUserId(String dealerUserId) {
        if (dealerUserId == null || dealerUserId.isBlank()) {
            throw new DomainValidationException("crm.error.dealerUserId.required");
        }
        this.dealerUserId = dealerUserId.trim();
    }

    public void setFullName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            throw new DomainValidationException("crm.error.fullName.required");
        }
        this.fullName = fullName.trim();
    }

    public void setStatus(String status) {
        if (status == null || status.isBlank()) {
            this.status = "NEW";
            return;
        }
        String normalized = status.trim().toUpperCase();
        if (!normalized.equals("NEW") && !normalized.equals("CONTACTED") && 
            !normalized.equals("TEST_DRIVE_SCHEDULED") && !normalized.equals("NEGOTIATING") && 
            !normalized.equals("CLOSED_WON") && !normalized.equals("CLOSED_LOST")) {
            throw new DomainValidationException("crm.error.prospectStatus.invalid");
        }
        this.status = normalized;
    }

    public void setSalesAgentId(String salesAgentId) {
        this.salesAgentId = salesAgentId != null ? salesAgentId.trim() : null;
    }

    public void addNote(String authorUserId, String noteText) {
        ProspectNote note = new ProspectNote(this.id.value(), authorUserId, noteText);
        this.notes.add(note);
    }
}
