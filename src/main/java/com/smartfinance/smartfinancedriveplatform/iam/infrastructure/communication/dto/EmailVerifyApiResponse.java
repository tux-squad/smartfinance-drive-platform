package com.smartfinance.smartfinancedriveplatform.iam.infrastructure.communication.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO representing the JSON response structure from EmailVerify.io single validation API.
 */
public record EmailVerifyApiResponse(
        @JsonProperty("email") String email,
        @JsonProperty("status") String status,
        @JsonProperty("sub_status") String subStatus,
        @JsonProperty("message") String message,
        @JsonProperty("error") String error
) {
}
