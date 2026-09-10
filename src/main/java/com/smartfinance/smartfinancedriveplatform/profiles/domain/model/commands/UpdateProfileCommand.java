package com.smartfinance.smartfinancedriveplatform.profiles.domain.model.commands;

import com.smartfinance.smartfinancedriveplatform.profiles.domain.model.valueobjects.ProfileId;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.valueobjects.Money;

import java.time.LocalDate;

/**
 * Command to request updating an existing customer profile.
 */
public record UpdateProfileCommand(
    ProfileId profileId,
    String email,
    String nationalId,
    String fullLegalNames,
    LocalDate dateOfBirth,
    String phoneCountryCode,
    String mobilePhone,
    Money monthlyIncome,
    String employmentStatus
) {}
