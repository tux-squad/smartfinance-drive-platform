package com.smartfinance.smartfinancedriveplatform.iam.infrastructure.communication.resources;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request DTO for Factiliza WhatsApp text message dispatch endpoint.
 */
public record FactilizaSendTextRequest(
        @JsonProperty("number") String number,
        @JsonProperty("text") String text
) {
}
