package com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * REST Response payload after an email verification code is dispatched.
 */
@Schema(description = "Response confirming email OTP dispatch")
public record EmailVerificationSentResource(
        @Schema(description = "Destination email address", example = "aldospeedcuber@gmail.com")
        String email,

        @Schema(description = "Masked email address for privacy", example = "a***r@gmail.com")
        String maskedEmail,

        @Schema(description = "Indicates whether the verification session is active", example = "true")
        boolean sessionActive,

        @Schema(description = "Time-to-live in seconds before code expiry", example = "600")
        int expiresInSeconds,

        @Schema(description = "User-friendly status message", example = "Código de verificación enviado exitosamente a tu correo electrónico.")
        String message
) {
}
