package com.smartfinance.smartfinancedriveplatform.iam.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "sales_agents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SalesAgentPersistenceEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "dealer_user_id", nullable = false)
    private String dealerUserId;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "email")
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "active", nullable = false)
    private boolean active;
}
