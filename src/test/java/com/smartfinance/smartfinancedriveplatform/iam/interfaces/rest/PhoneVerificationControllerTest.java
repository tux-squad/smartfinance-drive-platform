package com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest;

import com.smartfinance.smartfinancedriveplatform.iam.application.internal.commandservices.PhoneVerificationCommandService;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.VerifyFirebasePhoneTokenCommand;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.PhoneVerificationResult;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.PhoneVerificationStatus;
import com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.resources.PhoneVerificationConfirmationResource;
import com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.resources.VerifyFirebaseTokenResource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PhoneVerificationController Unit Tests")
class PhoneVerificationControllerTest {

    @Mock
    private PhoneVerificationCommandService phoneVerificationCommandService;

    @InjectMocks
    private PhoneVerificationController controller;

    @Test
    @DisplayName("Should return 200 OK with confirmation resource when verifying Firebase token")
    void shouldReturnOkWhenVerifyingFirebaseToken() {
        VerifyFirebaseTokenResource resource = new VerifyFirebaseTokenResource("sample-firebase-id-token");
        PhoneVerificationResult result = new PhoneVerificationResult(
                true, "51993913924", PhoneVerificationStatus.VERIFIED, Instant.now(), "firebase-verified-token-abc", "Verified via Firebase");

        when(phoneVerificationCommandService.handle(any(VerifyFirebasePhoneTokenCommand.class))).thenReturn(result);

        ResponseEntity<PhoneVerificationConfirmationResource> response = controller.verifyFirebaseToken(resource);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().verified());
        assertEquals("51993913924", response.getBody().phoneNumber());
        assertEquals("firebase-verified-token-abc", response.getBody().verificationToken());
    }
}
