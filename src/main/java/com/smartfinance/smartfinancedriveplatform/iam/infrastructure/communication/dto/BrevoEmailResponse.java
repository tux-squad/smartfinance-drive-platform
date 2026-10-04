package com.smartfinance.smartfinancedriveplatform.iam.infrastructure.communication.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO representing the JSON response structure from Brevo v3 Transactional Email API.
 */
public record BrevoEmailResponse(
        @JsonProperty("messageId") String messageId,
        @JsonProperty("message") String message,
        @JsonProperty("code") String code
) {
}
