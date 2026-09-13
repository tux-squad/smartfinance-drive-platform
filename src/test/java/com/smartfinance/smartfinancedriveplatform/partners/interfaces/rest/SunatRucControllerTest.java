package com.smartfinance.smartfinancedriveplatform.partners.interfaces.rest;

import com.smartfinance.smartfinancedriveplatform.partners.application.outboundservices.SunatRucVerifierService;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.valueobjects.SunatRucInfo;
import com.smartfinance.smartfinancedriveplatform.partners.interfaces.rest.resources.SunatRucResource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SunatRucController Unit Tests")
class SunatRucControllerTest {

    @Mock
    private SunatRucVerifierService sunatRucVerifierService;

    @InjectMocks
    private SunatRucController sunatRucController;

    @Test
    @DisplayName("Should return 200 OK on getRucInfo when valid RUC found")
    void shouldReturnOkOnValidRuc() {
        SunatRucInfo info = new SunatRucInfo(
                "20100070970",
                "SUPERMERCADOS PERUANOS S.A.",
                "ACTIVO",
                "HABIDO",
                "SOCIEDAD ANONIMA",
                "150130",
                "CAL. MORELLI 181",
                "4510"
        );

        when(sunatRucVerifierService.verifyRuc("20100070970")).thenReturn(Optional.of(info));

        ResponseEntity<SunatRucResource> response = sunatRucController.getRucInfo("20100070970");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("20100070970", response.getBody().ruc());
        assertEquals("SUPERMERCADOS PERUANOS S.A.", response.getBody().razonSocial());
        assertTrue(response.getBody().isActiveAndHabido());
        assertTrue(response.getBody().isAutomotiveCiiu());
    }

    @Test
    @DisplayName("Should return 404 Not Found when RUC does not exist")
    void shouldReturnNotFoundWhenRucDoesNotExist() {
        when(sunatRucVerifierService.verifyRuc("99999999999")).thenReturn(Optional.empty());

        ResponseEntity<SunatRucResource> response = sunatRucController.getRucInfo("99999999999");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }
}
