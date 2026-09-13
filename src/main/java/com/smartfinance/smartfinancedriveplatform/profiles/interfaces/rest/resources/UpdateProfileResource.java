package com.smartfinance.smartfinancedriveplatform.profiles.interfaces.rest.resources;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Resource DTO representing the request payload to update an existing profile.
 */
public record UpdateProfileResource(
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    String email,

    @NotBlank(message = "National ID (DNI/CE) is required")
    @Pattern(regexp = "^[0-9A-Za-z]{8,20}$", message = "National ID must be between 8 and 20 alphanumeric characters")
    String nationalId,

    @NotBlank(message = "Full legal names are required")
    @Size(max = 150, message = "Full legal names cannot exceed 150 characters")
    String fullLegalNames,

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    LocalDate dateOfBirth,

    @NotBlank(message = "Phone country code is required")
    @Size(max = 5, message = "Phone country code is invalid")
    String phoneCountryCode,

    @NotBlank(message = "Mobile phone is required")
    @Size(max = 15, message = "Mobile phone cannot exceed 15 digits")
    String mobilePhone,

    @NotNull(message = "Monthly income amount is required")
    @DecimalMin(value = "0.00", message = "Monthly income cannot be negative")
    BigDecimal monthlyIncomeAmount,

    @NotBlank(message = "Monthly income currency is required")
    @Size(min = 3, max = 3, message = "Monthly income currency must be a 3-letter code")
    String monthlyIncomeCurrency,

    @NotBlank(message = "Employment status is required")
    String employmentStatus
) {}
