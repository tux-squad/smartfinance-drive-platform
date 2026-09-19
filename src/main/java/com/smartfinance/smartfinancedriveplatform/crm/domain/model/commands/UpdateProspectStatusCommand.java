package com.smartfinance.smartfinancedriveplatform.crm.domain.model.commands;

import com.smartfinance.smartfinancedriveplatform.crm.domain.model.valueobjects.ProspectId;

public record UpdateProspectStatusCommand(
    ProspectId prospectId,
    String status
) {}
