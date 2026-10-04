package com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * REST Request resource for verifying an email OTP code.
 */
@Schema(description = "Payload to confirm an email verification code")
public record VerifyEmailCodeResource(
        @Schema(description = "Email address being verified", example = "aldospeedcuber@gmail.com")
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be a valid email address")
        String email,

        @Schema(description = "6-digit OTP code received by email", example = "849201")
        @NotBlank(message = "Verification code is required")
        @Pattern(regexp = "^\\d{6}$", message = "Verification code must consist of exactly 6 numeric digits")
        String code
) {
}
