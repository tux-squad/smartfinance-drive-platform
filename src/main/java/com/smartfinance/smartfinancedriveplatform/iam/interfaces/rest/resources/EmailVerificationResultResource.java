package com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/**
 * REST Response payload after email verification code is validated.
 */
@Schema(description = "Response confirming email verification result")
public record EmailVerificationResultResource(
        @Schema(description = "Verification success indicator", example = "true")
        boolean verified,

        @Schema(description = "Verified email address", example = "aldospeedcuber@gmail.com")
        String email,

        @Schema(description = "Lifecycle status of the session", example = "VERIFIED")
        String status,

        @Schema(description = "Instant of verification")
        Instant verifiedAt,

        @Schema(description = "Proof of verification token", example = "b47c0b02-5e36-4c3e-8f24-9121a97d8b8a")
        String verificationToken,

        @Schema(description = "User-friendly status message", example = "Correo electrónico verificado exitosamente.")
        String message
) {
}
