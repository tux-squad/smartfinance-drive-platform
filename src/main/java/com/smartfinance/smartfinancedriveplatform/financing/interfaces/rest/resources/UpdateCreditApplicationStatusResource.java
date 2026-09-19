package com.smartfinance.smartfinancedriveplatform.financing.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Resource DTO for updating status of a credit application.
 */
public record UpdateCreditApplicationStatusResource(
    @NotBlank(message = "Status is required")
    @Pattern(regexp = "^(?i)(PENDING|IN_REVIEW|PRE_APPROVED|REJECTED|DISBURSED)$", message = "Status must be PENDING, IN_REVIEW, PRE_APPROVED, REJECTED, or DISBURSED")
    String status,

    String notes
) {}
