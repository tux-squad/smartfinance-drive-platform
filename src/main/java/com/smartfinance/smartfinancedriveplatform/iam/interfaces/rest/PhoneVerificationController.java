package com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest;

import com.smartfinance.smartfinancedriveplatform.iam.application.internal.commandservices.PhoneVerificationCommandService;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.VerifyFirebasePhoneTokenCommand;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.PhoneVerificationResult;
import com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.resources.PhoneVerificationConfirmationResource;
import com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.resources.VerifyFirebaseTokenResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for phone verification operations via Firebase Phone Authentication.
 */
@RestController
@RequestMapping(value = "/api/v1/auth/phone-verification", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Authentication", description = "Endpoints for user authentication, registration, and identity verification")
public class PhoneVerificationController {

    private final PhoneVerificationCommandService phoneVerificationCommandService;

    public PhoneVerificationController(PhoneVerificationCommandService phoneVerificationCommandService) {
        this.phoneVerificationCommandService = phoneVerificationCommandService;
    }

    /**
     * POST /api/v1/auth/phone-verification/firebase (also accessible via /api/v1/auth/phone-verification)
     * Validates a client-side Firebase Phone Authentication ID Token (JWT) and issues a verification token.
     */
    @PostMapping({"", "/firebase"})
    @Operation(summary = "Verify phone number using Firebase Auth ID token",
               description = "Cryptographically validates a client-side Firebase Phone Auth ID Token against Google servers, extracts the verified phone number, and issues a verification token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Firebase ID token verified successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid or expired Firebase ID token, or missing phone number claim"),
            @ApiResponse(responseCode = "503", description = "Firebase authentication service unavailable")
    })
    public ResponseEntity<PhoneVerificationConfirmationResource> verifyFirebaseToken(
            @Valid @RequestBody VerifyFirebaseTokenResource resource) {
        String callerUserId = com.smartfinance.smartfinancedriveplatform.shared.infrastructure.security.SecurityUtils.getCurrentUserId().orElse(null);
        VerifyFirebasePhoneTokenCommand command = new VerifyFirebasePhoneTokenCommand(resource.firebaseIdToken(), callerUserId);
        PhoneVerificationResult result = phoneVerificationCommandService.handle(command);

        PhoneVerificationConfirmationResource response = new PhoneVerificationConfirmationResource(
                result.verified(),
                result.phoneNumber(),
                result.status().name(),
                result.verifiedAt(),
                result.verificationToken(),
                result.message()
        );
        return ResponseEntity.ok(response);
    }
}
