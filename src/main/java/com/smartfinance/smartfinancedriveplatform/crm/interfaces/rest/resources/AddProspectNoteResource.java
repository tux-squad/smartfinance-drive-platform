package com.smartfinance.smartfinancedriveplatform.crm.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;

public record AddProspectNoteResource(
    @NotBlank(message = "Note text is required")
    String noteText
) {}
