package com.smartfinance.smartfinancedriveplatform.profiles.infrastructure.persistence.jpa.entities;

import com.smartfinance.smartfinancedriveplatform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * JPA entity representing the 'profiles' table in the database.
 */
@Entity
@Table(name = "profiles")
@Getter
@Setter
@NoArgsConstructor
public class ProfilePersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "national_id")
    private String nationalId;

    @Column(name = "full_legal_names")
    private String fullLegalNames;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "phone_country_code", length = 10)
    private String phoneCountryCode;

    @Column(name = "mobile_phone", length = 30)
    private String mobilePhone;

    @Column(name = "monthly_income_amount", precision = 12, scale = 2)
    private BigDecimal monthlyIncomeAmount;

    @Column(name = "monthly_income_currency", length = 10)
    private String monthlyIncomeCurrency;

    @Column(name = "employment_status", length = 50)
    private String employmentStatus;
}
