package com.smartfinance.smartfinancedriveplatform.crm.interfaces.rest.resources;

import java.util.UUID;

public record CreateProspectResource(
        String fullName,
        String email,
        String phone,
        UUID interestedVehicleId,
        String salesAgentId
) {
}
