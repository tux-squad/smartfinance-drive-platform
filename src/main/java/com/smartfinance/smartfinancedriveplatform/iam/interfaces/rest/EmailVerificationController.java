package com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest;

import com.smartfinance.smartfinancedriveplatform.iam.application.internal.commandservices.EmailVerificationCommandService;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.SendEmailVerificationCommand;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.VerifyEmailCodeCommand;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.EmailVerificationResult;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.EmailVerificationSent;
import com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.resources.EmailVerificationResultResource;
import com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.resources.EmailVerificationSentResource;
import com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.resources.SendEmailVerificationResource;
import com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.resources.VerifyEmailCodeResource;
import com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.transform.EmailVerificationResourceAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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
 * REST Controller for email OTP verification flows.
 * Provides public endpoints to dispatch and confirm 6-digit OTP codes via Gmail SMTP.
 */
@RestController
@RequestMapping(value = "/api/v1/auth/email-verification", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Authentication", description = "Endpoints for user registration, authentication, token management, and email/phone verification")
public class EmailVerificationController {

    private final EmailVerificationCommandService emailVerificationCommandService;

    public EmailVerificationController(EmailVerificationCommandService emailVerificationCommandService) {
        this.emailVerificationCommandService = emailVerificationCommandService;
    }

    /**
     * Dispatches a 6-digit email OTP verification code to any valid email address.
     */
    @PostMapping("/send")
    @Operation(summary = "Send email verification OTP code",
            description = "Generates a cryptographically secure 6-digit OTP code and dispatches it via Gmail SMTP to the provided email address.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Verification code dispatched successfully",
                    content = @Content(schema = @Schema(implementation = EmailVerificationSentResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid email format or anti-abuse limit exceeded",
                    content = @Content),
            @ApiResponse(responseCode = "429", description = "Rate limit exceeded (max 10 requests/minute per IP)",
                    content = @Content)
    })
    public ResponseEntity<EmailVerificationSentResource> sendEmailVerification(
            @Valid @RequestBody SendEmailVerificationResource resource) {
        SendEmailVerificationCommand command = new SendEmailVerificationCommand(resource.email());
        EmailVerificationSent sent = emailVerificationCommandService.handle(command);
        return ResponseEntity.ok(EmailVerificationResourceAssembler.toResource(sent));
    }

    /**
     * Validates a 6-digit email OTP verification code.
     */
    @PostMapping("/verify")
    @Operation(summary = "Verify email OTP code",
            description = "Validates the 6-digit OTP code received by email in constant time and issues an email verification token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email verified successfully",
                    content = @Content(schema = @Schema(implementation = EmailVerificationResultResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid code, expired session, or max attempts exceeded",
                    content = @Content),
            @ApiResponse(responseCode = "429", description = "Rate limit exceeded",
                    content = @Content)
    })
    public ResponseEntity<EmailVerificationResultResource> verifyEmailCode(
            @Valid @RequestBody VerifyEmailCodeResource resource) {
        VerifyEmailCodeCommand command = new VerifyEmailCodeCommand(resource.email(), resource.code());
        EmailVerificationResult result = emailVerificationCommandService.handle(command);
        return ResponseEntity.ok(EmailVerificationResourceAssembler.toResource(result));
    }
}
