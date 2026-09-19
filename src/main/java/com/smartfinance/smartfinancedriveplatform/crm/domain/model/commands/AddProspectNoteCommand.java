package com.smartfinance.smartfinancedriveplatform.crm.domain.model.commands;

import com.smartfinance.smartfinancedriveplatform.crm.domain.model.valueobjects.ProspectId;

public record AddProspectNoteCommand(
    ProspectId prospectId,
    String authorUserId,
    String noteText
) {}
