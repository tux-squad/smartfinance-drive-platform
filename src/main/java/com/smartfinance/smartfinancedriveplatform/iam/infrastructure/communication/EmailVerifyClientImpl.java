package com.smartfinance.smartfinancedriveplatform.iam.infrastructure.communication;

import com.smartfinance.smartfinancedriveplatform.iam.application.outboundservices.EmailValidationService;
import com.smartfinance.smartfinancedriveplatform.iam.application.outboundservices.dto.EmailValidationResultDto;
import com.smartfinance.smartfinancedriveplatform.iam.infrastructure.communication.dto.EmailVerifyApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Infrastructure implementation of EmailValidationService interfacing with EmailVerify.io REST API via HTTPS (Port 443).
 */
@Service
public class EmailVerifyClientImpl implements EmailValidationService {

    private static final Logger log = LoggerFactory.getLogger(EmailVerifyClientImpl.class);

    private final RestClient restClient;
    private final String apiKey;
    private final String baseUrl;

    public EmailVerifyClientImpl(
            RestClient.Builder restClientBuilder,
            @Value("${emailverify.api-key:}") String apiKey,
            @Value("${emailverify.base-url:https://app.emailverify.io}") String baseUrl) {
        this.apiKey = apiKey != null ? apiKey.trim() : "";
        this.baseUrl = baseUrl != null && !baseUrl.isBlank() ? baseUrl.trim() : "https://app.emailverify.io";
        this.restClient = restClientBuilder.baseUrl(this.baseUrl).build();
    }

    public EmailVerifyClientImpl(RestClient restClient, String apiKey, String baseUrl) {
        this.restClient = restClient;
        this.apiKey = apiKey != null ? apiKey.trim() : "";
        this.baseUrl = baseUrl != null && !baseUrl.isBlank() ? baseUrl.trim() : "https://app.emailverify.io";
    }

    @Override
    public EmailValidationResultDto validateEmail(String email) {
        if (email == null || email.isBlank()) {
            return new EmailValidationResultDto("", "invalid", "empty_email", false);
        }

        String targetEmail = email.trim().toLowerCase();

        if (apiKey.isBlank()) {
            log.warn("EmailVerify API key is not configured (EMAILVERIFY_API_KEY is empty). Bypassing real-time verification for [{}]", targetEmail);
            return EmailValidationResultDto.fallbackValid(targetEmail);
        }

        try {
            String uri = UriComponentsBuilder.fromPath("/api/v1/validate")
                    .queryParam("key", apiKey)
                    .queryParam("email", targetEmail)
                    .toUriString();

            log.info("Dispatching real-time email verification request to EmailVerify.io for [{}]", targetEmail);

            EmailVerifyApiResponse response = restClient.get()
                    .uri(uri)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(EmailVerifyApiResponse.class);

            if (response == null) {
                log.warn("Received empty response from EmailVerify.io for [{}]", targetEmail);
                return EmailValidationResultDto.fallbackValid(targetEmail);
            }

            log.info("EmailVerify.io response for [{}]: status=[{}], sub_status=[{}]",
                    targetEmail, response.status(), response.subStatus());

            return EmailValidationResultDto.valid(targetEmail, response.status(), response.subStatus());

        } catch (Exception e) {
            log.error("Failed to query EmailVerify.io API for [{}]: {}", targetEmail, e.getMessage());
            // Graceful fallback to avoid blocking users if external verification provider is temporarily unreachable
            return EmailValidationResultDto.fallbackValid(targetEmail);
        }
    }
}
