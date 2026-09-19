package com.smartfinance.smartfinancedriveplatform.crm.infrastructure.persistence.jpa.entities;

import com.smartfinance.smartfinancedriveplatform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "prospects", indexes = {
        @Index(name = "idx_prospects_dealer", columnList = "dealer_user_id"),
        @Index(name = "idx_prospects_buyer", columnList = "buyer_user_id")
})
@Getter
@Setter
@NoArgsConstructor
public class ProspectPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Column(name = "dealer_user_id", nullable = false)
    private String dealerUserId;

    @Column(name = "buyer_user_id")
    private String buyerUserId;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "email")
    private String email;

    @Column(name = "phone", length = 30)
    private String phone;

    @Column(name = "interested_vehicle_id")
    private UUID interestedVehicleId;

    @Column(name = "status", nullable = false, length = 30)
    private String status = "NEW";

    @Column(name = "sales_agent_id")
    private String salesAgentId;

    @OneToMany(mappedBy = "prospect", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private List<ProspectNotePersistenceEntity> notes = new ArrayList<>();
}
