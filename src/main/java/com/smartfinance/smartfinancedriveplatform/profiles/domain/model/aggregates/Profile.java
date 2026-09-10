package com.smartfinance.smartfinancedriveplatform.profiles.domain.model.aggregates;

import com.smartfinance.smartfinancedriveplatform.profiles.domain.model.valueobjects.ProfileId;
import com.smartfinance.smartfinancedriveplatform.profiles.domain.model.valueobjects.UserId;
import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.valueobjects.Money;

import lombok.Getter;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Profile aggregate root.
 * Represents customer personal, employment, and financial profile details.
 */
@Getter
public class Profile extends AbstractDomainAggregateRoot<Profile> {

    private final ProfileId id;
    private UserId userId;
    private String email;
    private String nationalId;
    private String fullLegalNames;
    private LocalDate dateOfBirth;
    private String phoneCountryCode;
    private String mobilePhone;
    private Money monthlyIncome;
    private String employmentStatus; // EMPLOYED, INDEPENDENT, UNEMPLOYED, RETIRED

    /**
     * Constructor for reconstituting the aggregate from persistence.
     */
    public Profile(ProfileId id, UserId userId, String email, String nationalId, 
                   String fullLegalNames, LocalDate dateOfBirth, String phoneCountryCode, 
                   String mobilePhone, Money monthlyIncome, String employmentStatus) {
        this.id = id;
        this.userId = userId;
        this.email = email;
        this.nationalId = nationalId;
        this.fullLegalNames = fullLegalNames;
        this.dateOfBirth = dateOfBirth;
        this.phoneCountryCode = phoneCountryCode;
        this.mobilePhone = mobilePhone;
        this.monthlyIncome = monthlyIncome;
        this.employmentStatus = employmentStatus;
    }

    /**
     * Constructor for creating a new Profile.
     */
    public Profile(UserId userId, String email, String nationalId, String fullLegalNames, 
                   LocalDate dateOfBirth, String phoneCountryCode, String mobilePhone, 
                   Money monthlyIncome, String employmentStatus) {
        this.id = new ProfileId(UUID.randomUUID());
        setUserId(userId);
        setEmail(email);
        setNationalId(nationalId);
        setFullLegalNames(fullLegalNames);
        setDateOfBirth(dateOfBirth);
        setPhoneCountryCode(phoneCountryCode);
        setMobilePhone(mobilePhone);
        setMonthlyIncome(monthlyIncome);
        setEmploymentStatus(employmentStatus);
    }

    public void setUserId(UserId userId) {
        if (userId == null) {
            throw new DomainValidationException("profiles.error.userId.required");
        }
        this.userId = userId;
    }

    public void setEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new DomainValidationException("profiles.error.email.required");
        }
        this.email = email.trim().toLowerCase();
    }

    public void setNationalId(String nationalId) {
        if (nationalId != null && nationalId.isBlank()) {
            this.nationalId = null;
        } else {
            this.nationalId = nationalId != null ? nationalId.trim() : null;
        }
    }

    public void setFullLegalNames(String fullLegalNames) {
        if (fullLegalNames != null && fullLegalNames.isBlank()) {
            this.fullLegalNames = null;
        } else {
            this.fullLegalNames = fullLegalNames != null ? fullLegalNames.trim() : null;
        }
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        if (dateOfBirth != null && dateOfBirth.isAfter(LocalDate.now())) {
            throw new DomainValidationException("profiles.error.dateOfBirth.invalid");
        }
        this.dateOfBirth = dateOfBirth;
    }

    public void setPhoneCountryCode(String phoneCountryCode) {
        this.phoneCountryCode = (phoneCountryCode != null && !phoneCountryCode.isBlank()) 
            ? phoneCountryCode.trim() 
            : "+51";
    }

    public void setMobilePhone(String mobilePhone) {
        this.mobilePhone = (mobilePhone != null && !mobilePhone.isBlank()) 
            ? mobilePhone.trim() 
            : null;
    }

    public void setMonthlyIncome(Money monthlyIncome) {
        this.monthlyIncome = monthlyIncome;
    }

    public void setEmploymentStatus(String employmentStatus) {
        if (employmentStatus != null && !employmentStatus.isBlank()) {
            this.employmentStatus = employmentStatus.trim().toUpperCase();
        } else {
            this.employmentStatus = null;
        }
    }

    /**
     * Updates details of the profile.
     */
    public void updateDetails(String email, String nationalId, String fullLegalNames, 
                              LocalDate dateOfBirth, String phoneCountryCode, String mobilePhone, 
                              Money monthlyIncome, String employmentStatus) {
        setEmail(email);
        setNationalId(nationalId);
        setFullLegalNames(fullLegalNames);
        setDateOfBirth(dateOfBirth);
        setPhoneCountryCode(phoneCountryCode);
        setMobilePhone(mobilePhone);
        setMonthlyIncome(monthlyIncome);
        setEmploymentStatus(employmentStatus);
    }
}
