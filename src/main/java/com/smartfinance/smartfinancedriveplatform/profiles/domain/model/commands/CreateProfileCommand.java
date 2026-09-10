package com.smartfinance.smartfinancedriveplatform.profiles.domain.model.commands;

import com.smartfinance.smartfinancedriveplatform.profiles.domain.model.valueobjects.UserId;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.valueobjects.Money;

import java.time.LocalDate;

/**
 * Command to request the creation of a new customer profile.
 */
public record CreateProfileCommand(
    UserId userId,
    String email,
    String nationalId,
    String fullLegalNames,
    LocalDate dateOfBirth,
    String phoneCountryCode,
    String mobilePhone,
    Money monthlyIncome,
    String employmentStatus
) {}
