package com.smartfinance.smartfinancedriveplatform.iam.infrastructure.communication;

import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.ExternalServiceUnavailableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@DisplayName("FactilizaWhatsAppSenderServiceImpl Unit Tests")
class FactilizaWhatsAppSenderServiceImplTest {

    private MockRestServiceServer mockServer;
    private FactilizaWhatsAppSenderServiceImpl service;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://apiwsp.factiliza.com");
        mockServer = MockRestServiceServer.bindTo(builder).build();
        service = new FactilizaWhatsAppSenderServiceImpl(builder.build(), "smartfinance-test");
    }

    @Test
    @DisplayName("Should successfully dispatch verification code to WhatsApp API")
    void shouldSuccessfullyDispatchVerificationCode() {
        String jsonResponse = """
                {
                  "status": 200,
                  "success": true,
                  "message": "Mensaje enviado con Exito!"
                }
                """;

        mockServer.expect(requestTo("https://apiwsp.factiliza.com/v1/message/sendtext/smartfinance-test"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.number").value("51993913924"))
                .andExpect(jsonPath("$.text").isNotEmpty())
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        assertDoesNotThrow(() -> service.sendVerificationCode("51993913924", "123456"));
        mockServer.verify();
    }

    @Test
    @DisplayName("Should throw DomainValidationException when gateway reports success=false")
    void shouldThrowExceptionWhenGatewayReturnsFalse() {
        String jsonFailure = """
                {
                  "status": 400,
                  "success": false,
                  "message": "Instancia desconectada o número no existe"
                }
                """;

        mockServer.expect(requestTo("https://apiwsp.factiliza.com/v1/message/sendtext/smartfinance-test"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(jsonFailure, MediaType.APPLICATION_JSON));

        assertThrows(DomainValidationException.class, () -> service.sendVerificationCode("51993913924", "123456"));
        mockServer.verify();
    }

    @Test
    @DisplayName("Should throw ExternalServiceUnavailableException on HTTP 500 server error")
    void shouldThrowExceptionOnServerError() {
        mockServer.expect(requestTo("https://apiwsp.factiliza.com/v1/message/sendtext/smartfinance-test"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withServerError());

        assertThrows(ExternalServiceUnavailableException.class, () -> service.sendVerificationCode("51993913924", "123456"));
        mockServer.verify();
    }

    @Test
    @DisplayName("Should fail-closed and throw ExternalServiceUnavailableException in production when apiKey is blank and allowEmulated is false")
    void shouldFailClosedWhenApiKeyIsBlankAndEmulationDisabled() {
        FactilizaWhatsAppSenderServiceImpl unconfiguredService = new FactilizaWhatsAppSenderServiceImpl(
                "https://apiwsp.factiliza.com", "smartfinance-test", "", false, 5
        );

        assertThrows(ExternalServiceUnavailableException.class, () -> unconfiguredService.sendVerificationCode("51993913924", "123456"));
    }

    @Test
    @DisplayName("Should emulate dispatch without calling network when apiKey is blank and allowEmulated is true")
    void shouldEmulateWhenApiKeyIsBlankAndEmulationAllowed() {
        FactilizaWhatsAppSenderServiceImpl emulatedService = new FactilizaWhatsAppSenderServiceImpl(
                "https://apiwsp.factiliza.com", "smartfinance-test", "", true, 5
        );

        assertDoesNotThrow(() -> emulatedService.sendVerificationCode("51993913924", "123456"));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException on null or blank arguments")
    void shouldThrowIllegalArgumentOnBlankInputs() {
        assertThrows(IllegalArgumentException.class, () -> service.sendVerificationCode(null, "123456"));
        assertThrows(IllegalArgumentException.class, () -> service.sendVerificationCode("51993913924", null));
        assertThrows(IllegalArgumentException.class, () -> service.sendVerificationCode("51993913924", "   "));
    }
}
