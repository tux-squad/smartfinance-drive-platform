package com.smartfinance.smartfinancedriveplatform.iam.infrastructure.communication;

import com.smartfinance.smartfinancedriveplatform.iam.application.outboundservices.dto.EmailValidationResultDto;
import com.smartfinance.smartfinancedriveplatform.iam.infrastructure.communication.dto.EmailVerifyApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmailVerifyClientImpl Unit Tests")
@SuppressWarnings({"rawtypes", "unchecked"})
class EmailVerifyClientImplTest {

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private RestClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    private EmailVerifyClientImpl emailVerifyClient;

    @BeforeEach
    void setUp() {
        emailVerifyClient = new EmailVerifyClientImpl(restClient, "test-api-key", "https://app.emailverify.io");
    }

    @Test
    @DisplayName("Should return valid result when EmailVerify.io returns valid status")
    void shouldReturnValidResultWhenApiReturnsValid() {
        EmailVerifyApiResponse mockResponse = new EmailVerifyApiResponse(
                "jhon123@gmail.com", "valid", "permitted", null, null
        );

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.accept(org.springframework.http.MediaType.APPLICATION_JSON)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(EmailVerifyApiResponse.class)).thenReturn(mockResponse);

        EmailValidationResultDto result = emailVerifyClient.validateEmail("jhon123@gmail.com");

        assertThat(result).isNotNull();
        assertThat(result.email()).isEqualTo("jhon123@gmail.com");
        assertThat(result.isValid()).isTrue();
        assertThat(result.status()).isEqualTo("valid");
        assertThat(result.subStatus()).isEqualTo("permitted");
    }

    @Test
    @DisplayName("Should return fallback valid when API key is empty")
    void shouldReturnFallbackValidWhenApiKeyIsEmpty() {
        EmailVerifyClientImpl clientWithoutKey = new EmailVerifyClientImpl(restClient, "", "https://app.emailverify.io");

        EmailValidationResultDto result = clientWithoutKey.validateEmail("user@example.com");

        assertThat(result).isNotNull();
        assertThat(result.isValid()).isTrue();
        assertThat(result.subStatus()).isEqualTo("bypassed");
    }

    @Test
    @DisplayName("Should return invalid result when email is empty")
    void shouldReturnInvalidWhenEmailIsEmpty() {
        EmailValidationResultDto result = emailVerifyClient.validateEmail("");

        assertThat(result).isNotNull();
        assertThat(result.isValid()).isFalse();
        assertThat(result.subStatus()).isEqualTo("empty_email");
    }
}
