package com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * REST Request resource for requesting an email OTP verification code.
 */
@Schema(description = "Payload to request an email verification code")
public record SendEmailVerificationResource(
        @Schema(description = "Destination email address to verify", example = "aldospeedcuber@gmail.com")
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be a valid email address")
        String email
) {
}
