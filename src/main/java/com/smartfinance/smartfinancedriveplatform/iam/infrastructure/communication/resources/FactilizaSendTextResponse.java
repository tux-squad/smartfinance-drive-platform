package com.smartfinance.smartfinancedriveplatform.iam.infrastructure.communication.resources;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response DTO from Factiliza WhatsApp text message dispatch endpoint.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record FactilizaSendTextResponse(
        @JsonProperty("status") Integer status,
        @JsonProperty("success") Boolean success,
        @JsonProperty("message") String message
) {
}
