package com.smartfinance.smartfinancedriveplatform.crm.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdateProspectStatusResource(
    @NotBlank(message = "Status is required")
    @Pattern(regexp = "^(?i)(NEW|CONTACTED|TEST_DRIVE_SCHEDULED|NEGOTIATING|CLOSED_WON|CLOSED_LOST)$", 
             message = "Invalid status")
    String status
) {}
