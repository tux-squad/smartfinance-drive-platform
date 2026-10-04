package com.smartfinance.smartfinancedriveplatform.iam.infrastructure.communication.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * DTO representing the JSON request payload for Brevo v3 Transactional Email API (POST /v3/smtp/email).
 */
public record BrevoEmailRequest(
        @JsonProperty("sender") BrevoSender sender,
        @JsonProperty("to") List<BrevoRecipient> to,
        @JsonProperty("subject") String subject,
        @JsonProperty("htmlContent") String htmlContent
) {
    public record BrevoSender(
            @JsonProperty("name") String name,
            @JsonProperty("email") String email
    ) {
    }

    public record BrevoRecipient(
            @JsonProperty("email") String email,
            @JsonProperty("name") String name
    ) {
    }
}
