package com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest;

import com.smartfinance.smartfinancedriveplatform.iam.application.internal.commandservices.PhoneVerificationCommandService;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.aggregates.PhoneVerificationSession;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.SendPhoneVerificationCodeCommand;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.VerifyPhoneCodeCommand;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.PhoneVerificationResult;
import com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.resources.PhoneVerificationConfirmationResource;
import com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.resources.PhoneVerificationSessionResource;
import com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.resources.SendPhoneVerificationResource;
import com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.resources.VerifyPhoneCodeResource;
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

import java.time.Duration;
import java.time.Instant;

/**
 * REST controller for phone verification operations via WhatsApp OTP.
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
     * POST /api/v1/auth/phone-verification/send
     * Generates a 6-digit OTP code and dispatches it to the recipient's WhatsApp.
     */
    @PostMapping("/send")
    @Operation(summary = "Send phone verification code via WhatsApp",
               description = "Dispatches a cryptographically generated 6-digit OTP code to the supplied Peruvian mobile phone number via Factiliza WhatsApp API.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Verification code dispatched successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid phone number or active resend cooldown"),
            @ApiResponse(responseCode = "429", description = "Too many requests (rate limit exceeded)")
    })
    public ResponseEntity<PhoneVerificationSessionResource> sendVerificationCode(
            @Valid @RequestBody SendPhoneVerificationResource resource) {
        String userId = com.smartfinance.smartfinancedriveplatform.shared.infrastructure.security.SecurityUtils.getCurrentUserId().orElse(null);
        SendPhoneVerificationCodeCommand command = new SendPhoneVerificationCodeCommand(resource.phoneNumber(), userId);
        PhoneVerificationSession session = phoneVerificationCommandService.handle(command);

        long expiresInSeconds = Math.max(0, Duration.between(Instant.now(), session.getExpiresAt()).getSeconds());
        PhoneVerificationSessionResource response = new PhoneVerificationSessionResource(
                session.getId(),
                session.getPhoneNumber().getMasked(),
                session.getStatus().name(),
                session.getExpiresAt(),
                expiresInSeconds,
                "Código de verificación enviado exitosamente por WhatsApp"
        );
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/v1/auth/phone-verification/verify
     * Confirms the OTP code submitted by the user.
     */
    @PostMapping("/verify")
    @Operation(summary = "Verify phone verification code",
               description = "Verifies the submitted 6-digit OTP code in constant time against the active verification session.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Phone number verified successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid code, expired session, or max attempts reached"),
            @ApiResponse(responseCode = "429", description = "Too many requests (rate limit exceeded)")
    })
    public ResponseEntity<PhoneVerificationConfirmationResource> verifyCode(
            @Valid @RequestBody VerifyPhoneCodeResource resource) {
        VerifyPhoneCodeCommand command = new VerifyPhoneCodeCommand(resource.phoneNumber(), resource.code());
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
