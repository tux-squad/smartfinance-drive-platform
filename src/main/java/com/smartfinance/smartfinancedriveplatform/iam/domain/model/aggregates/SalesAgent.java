package com.smartfinance.smartfinancedriveplatform.iam.domain.model.aggregates;

import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.SalesAgentId;
import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;

import java.util.UUID;

/**
 * SalesAgent Aggregate Root for Dealership Sales Team Management.
 */
@Getter
public class SalesAgent extends AbstractDomainAggregateRoot<SalesAgent> {

    private final SalesAgentId id;
    private String dealerUserId;
    private String fullName;
    private String email;
    private String phone;
    private boolean active;

    public SalesAgent(SalesAgentId id, String dealerUserId, String fullName, String email, String phone, boolean active) {
        this.id = id;
        setDealerUserId(dealerUserId);
        setFullName(fullName);
        this.email = email != null ? email.trim() : null;
        this.phone = phone != null ? phone.trim() : null;
        this.active = active;
    }

    public SalesAgent(String dealerUserId, String fullName, String email, String phone) {
        this(new SalesAgentId(UUID.randomUUID()), dealerUserId, fullName, email, phone, true);
    }

    public void setDealerUserId(String dealerUserId) {
        if (dealerUserId == null || dealerUserId.isBlank()) {
            throw new DomainValidationException("iam.error.salesAgent.dealerUserId.required");
        }
        this.dealerUserId = dealerUserId.trim();
    }

    public void setFullName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            throw new DomainValidationException("iam.error.salesAgent.fullName.required");
        }
        this.fullName = fullName.trim();
    }

    public void updateDetails(String fullName, String email, String phone, boolean active) {
        setFullName(fullName);
        this.email = email != null ? email.trim() : null;
        this.phone = phone != null ? phone.trim() : null;
        this.active = active;
    }
}
