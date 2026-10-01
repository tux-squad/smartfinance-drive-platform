package com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest;

import com.smartfinance.smartfinancedriveplatform.iam.application.internal.commandservices.PhoneVerificationCommandService;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.aggregates.PhoneVerificationSession;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.SendPhoneVerificationCodeCommand;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.VerifyPhoneCodeCommand;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.PhoneNumber;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.PhoneVerificationResult;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.PhoneVerificationStatus;
import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import com.smartfinance.smartfinancedriveplatform.shared.interfaces.rest.setup.GlobalExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {PhoneVerificationController.class, GlobalExceptionHandler.class})
@Import(PhoneVerificationWebMvcTest.SecurityTestConfig.class)
@DisplayName("PhoneVerificationController WebMvc Tests")
class PhoneVerificationWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PhoneVerificationCommandService commandService;

    @MockitoBean
    private com.smartfinance.smartfinancedriveplatform.iam.infrastructure.authorization.sbc.pipeline.JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private com.smartfinance.smartfinancedriveplatform.iam.infrastructure.authorization.sbc.pipeline.RateLimitingFilter rateLimitingFilter;

    @MockitoBean
    private org.springframework.cache.CacheManager cacheManager;

    @org.junit.jupiter.api.BeforeEach
    void setUp() throws Exception {
        org.mockito.Mockito.doAnswer(invocation -> {
            jakarta.servlet.ServletRequest req = invocation.getArgument(0);
            jakarta.servlet.ServletResponse res = invocation.getArgument(1);
            jakarta.servlet.FilterChain chain = invocation.getArgument(2);
            chain.doFilter(req, res);
            return null;
        }).when(jwtAuthenticationFilter).doFilter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());

        org.mockito.Mockito.doAnswer(invocation -> {
            jakarta.servlet.ServletRequest req = invocation.getArgument(0);
            jakarta.servlet.ServletResponse res = invocation.getArgument(1);
            jakarta.servlet.FilterChain chain = invocation.getArgument(2);
            chain.doFilter(req, res);
            return null;
        }).when(rateLimitingFilter).doFilter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @TestConfiguration
    static class SecurityTestConfig {
        @Bean
        public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
            http
                    .csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(auth -> auth.requestMatchers("/api/v1/auth/**").permitAll().anyRequest().authenticated());
            return http.build();
        }
    }

    @Test
    @DisplayName("POST /api/v1/auth/phone-verification/send should return 200 OK for valid phone number")
    void sendPhoneVerificationSuccess() throws Exception {
        UUID sessionId = UUID.randomUUID();
        PhoneNumber phone = new PhoneNumber("51993913924");
        Instant now = Instant.now();
        PhoneVerificationSession session = new PhoneVerificationSession(sessionId, null, phone, "hash", 0, PhoneVerificationStatus.PENDING, now, now.plus(Duration.ofMinutes(5)), null);

        when(commandService.handle(any(SendPhoneVerificationCodeCommand.class))).thenReturn(session);

        String payload = """
                {
                    "phoneNumber": "+51 993913924"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/phone-verification/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value(sessionId.toString()))
                .andExpect(jsonPath("$.phoneNumber").value("51993****24"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/phone-verification/send should return 400 Bad Request for invalid phone format")
    void sendPhoneVerificationInvalidPhone() throws Exception {
        String payload = """
                {
                    "phoneNumber": "12345"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/phone-verification/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/auth/phone-verification/verify should return 200 OK on successful verification")
    void verifyPhoneCodeSuccess() throws Exception {
        String verificationToken = UUID.randomUUID().toString();
        PhoneVerificationResult result = new PhoneVerificationResult(
                true, "51993913924", PhoneVerificationStatus.VERIFIED, Instant.now(), verificationToken, "Phone number successfully verified"
        );

        when(commandService.handle(any(VerifyPhoneCodeCommand.class))).thenReturn(result);

        String payload = """
                {
                    "phoneNumber": "+51 993913924",
                    "code": "123456"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/phone-verification/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verified").value(true))
                .andExpect(jsonPath("$.phoneNumber").value("51993913924"))
                .andExpect(jsonPath("$.status").value("VERIFIED"))
                .andExpect(jsonPath("$.verificationToken").value(verificationToken));
    }

    @Test
    @DisplayName("POST /api/v1/auth/phone-verification/verify should return 400 Bad Request when code is wrong")
    void verifyPhoneCodeWrongCode() throws Exception {
        when(commandService.handle(any(VerifyPhoneCodeCommand.class)))
                .thenThrow(new DomainValidationException("iam.error.phoneVerification.invalidCode"));

        String payload = """
                {
                    "phoneNumber": "+51 993913924",
                    "code": "999999"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/phone-verification/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }
}
