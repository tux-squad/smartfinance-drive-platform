package com.smartfinance.smartfinancedriveplatform.iam.infrastructure.communication;

import com.smartfinance.smartfinancedriveplatform.iam.application.outboundservices.PhoneVerificationSenderService;
import com.smartfinance.smartfinancedriveplatform.iam.infrastructure.communication.resources.FactilizaSendTextRequest;
import com.smartfinance.smartfinancedriveplatform.iam.infrastructure.communication.resources.FactilizaSendTextResponse;
import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.ExternalServiceUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

/**
 * Outbound adapter for sending OTP verification codes via Factiliza WhatsApp API.
 * Uses Spring RestClient with connection timeouts, resilient exception mapping, and masked audit logging.
 */
@Service
public class FactilizaWhatsAppSenderServiceImpl implements PhoneVerificationSenderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FactilizaWhatsAppSenderServiceImpl.class);

    private final RestClient restClient;
    private final String instanceName;
    private final String apiKey;
    private final boolean allowEmulated;
    private final int otpTtlMinutes;

    @Autowired
    public FactilizaWhatsAppSenderServiceImpl(
            @Value("${factiliza.whatsapp.base-url:https://apiwsp.factiliza.com}") String baseUrl,
            @Value("${factiliza.whatsapp.instance-name:smartfinance}") String instanceName,
            @Value("${factiliza.whatsapp.api-key:${factiliza.api-key:}}") String apiKey,
            @Value("${app.whatsapp.allow-emulated:false}") boolean allowEmulated,
            @Value("${factiliza.whatsapp.otp-ttl-minutes:5}") int otpTtlMinutes) {
        this.instanceName = (instanceName != null && !instanceName.isBlank()) ? instanceName.trim() : "smartfinance";
        this.apiKey = apiKey != null ? apiKey.trim() : "";
        this.allowEmulated = allowEmulated;
        this.otpTtlMinutes = otpTtlMinutes > 0 ? otpTtlMinutes : 5;

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(4000);
        requestFactory.setReadTimeout(7000);

        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + this.apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    // Constructor for testing with custom or mocked RestClient
    public FactilizaWhatsAppSenderServiceImpl(RestClient restClient, String instanceName, boolean allowEmulated, int otpTtlMinutes) {
        this.restClient = restClient;
        this.instanceName = instanceName != null ? instanceName : "smartfinance";
        this.apiKey = "test-key";
        this.allowEmulated = allowEmulated;
        this.otpTtlMinutes = otpTtlMinutes > 0 ? otpTtlMinutes : 5;
    }

    public FactilizaWhatsAppSenderServiceImpl(RestClient restClient, String instanceName) {
        this(restClient, instanceName, false, 5);
    }

    @Override
    public void sendVerificationCode(String fullPhoneNumber, String code) {
        if (fullPhoneNumber == null || fullPhoneNumber.isBlank()) {
            throw new IllegalArgumentException("Phone number cannot be null or blank");
        }
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Verification code cannot be null or blank");
        }

        String maskedPhone = maskPhoneNumber(fullPhoneNumber);

        if (apiKey.isBlank()) {
            if (allowEmulated) {
                LOGGER.info("Factiliza WhatsApp API key is not configured. Emulated WhatsApp OTP dispatch for [{}]", maskedPhone);
                return;
            }
            LOGGER.error("Factiliza WhatsApp API key is not configured and WhatsApp emulation is disabled. Failed to dispatch OTP to [{}]", maskedPhone);
            throw new ExternalServiceUnavailableException("iam.error.phoneVerification.serviceUnavailable");
        }

        String messageText = String.format(
                "🚗 *SmartFinance Drive*\n\nTu código de verificación es: *%s*\n\n(Válido por %d minutos. No compartas este código con nadie por seguridad).",
                code,
                otpTtlMinutes
        );

        FactilizaSendTextRequest requestPayload = new FactilizaSendTextRequest(fullPhoneNumber, messageText);

        try {
            LOGGER.info("Dispatching WhatsApp verification OTP to recipient [{}] via instance [{}]", maskedPhone, instanceName);

            var response = restClient.post()
                    .uri("/v1/message/sendtext/{instanceName}", instanceName)
                    .body(requestPayload)
                    .retrieve()
                    .body(FactilizaSendTextResponse.class);

            if (response != null && Boolean.TRUE.equals(response.success())) {
                LOGGER.info("WhatsApp verification OTP successfully dispatched to recipient [{}]", maskedPhone);
            } else {
                String errorMsg = response != null ? response.message() : "Unknown gateway error";
                LOGGER.error("Factiliza WhatsApp gateway returned failure for recipient [{}]: {}", maskedPhone, errorMsg);
                throw new DomainValidationException("iam.error.phoneVerification.dispatchFailed");
            }
        } catch (RestClientResponseException e) {
            LOGGER.error("HTTP error response from Factiliza WhatsApp API for recipient [{}]: Status {} Body {}",
                    maskedPhone, e.getStatusCode(), e.getResponseBodyAsString());
            throw new ExternalServiceUnavailableException(
                    "iam.error.phoneVerification.gatewayError",
                    "El servicio de mensajería no pudo completar el envío del código. Inténtelo más tarde.",
                    e
            );
        } catch (DomainValidationException | ExternalServiceUnavailableException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.error("Unexpected failure connecting to Factiliza WhatsApp API for recipient [{}]: {}",
                    maskedPhone, e.getMessage());
            throw new ExternalServiceUnavailableException(
                    "iam.error.phoneVerification.serviceUnavailable",
                    "Error de conexión con la pasarela de verificación. Inténtelo más tarde.",
                    e
            );
        }
    }

    private String maskPhoneNumber(String phone) {
        if (phone == null || phone.length() < 7) {
            return "***";
        }
        return phone.substring(0, 5) + "****" + phone.substring(phone.length() - 2);
    }
}
