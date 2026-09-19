package com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.resources;

import java.util.UUID;

public record ReassignLeadsResource(
        UUID targetAgentId
) {
}
