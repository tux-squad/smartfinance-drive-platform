package com.smartfinance.smartfinancedriveplatform.profiles.interfaces.rest.resources;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Resource DTO representing the request payload to create a new profile.
 */
public record CreateProfileResource(
    UUID userId,
    String email,
    String nationalId,
    String fullLegalNames,
    LocalDate dateOfBirth,
    String phoneCountryCode,
    String mobilePhone,
    BigDecimal monthlyIncomeAmount,
    String monthlyIncomeCurrency,
    String employmentStatus
) {}
