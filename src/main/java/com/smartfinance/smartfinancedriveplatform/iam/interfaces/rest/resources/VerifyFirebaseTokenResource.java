package com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * REST Request resource for verifying a Firebase Phone Authentication ID Token (JWT).
 */
public record VerifyFirebaseTokenResource(
        @NotBlank(message = "Firebase ID token is required")
        @Schema(description = "Raw Firebase ID Token (JWT) returned by confirmationResult.confirm(code) on client", example = "eyJhbGciOiJSUzI1NiIs...")
        String firebaseIdToken
) {
}
