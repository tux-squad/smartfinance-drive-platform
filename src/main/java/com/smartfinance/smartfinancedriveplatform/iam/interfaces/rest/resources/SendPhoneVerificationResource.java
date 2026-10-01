package com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * REST Request resource for requesting a phone verification code via WhatsApp.
 */
public record SendPhoneVerificationResource(
        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "^(\\+?51)?[\\s\\-()]?9\\d{8}$", message = "Phone number must be a valid 9-digit Peruvian mobile number (e.g. 993913924, +51 993913924)")
        String phoneNumber
) {
}
