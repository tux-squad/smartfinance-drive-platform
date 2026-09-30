package com.smartfinance.smartfinancedriveplatform.partners.interfaces.rest;

import com.smartfinance.smartfinancedriveplatform.partners.application.queryservices.CorporateLookupQueryService;
import com.smartfinance.smartfinancedriveplatform.partners.interfaces.rest.resources.CorporateLookupResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("CorporateVerificationController Unit Tests")
class CorporateVerificationControllerTest {

    private CorporateLookupQueryService queryService;
    private CorporateVerificationController controller;

    @BeforeEach
    void setUp() {
        queryService = mock(CorporateLookupQueryService.class);
        controller = new CorporateVerificationController(queryService);
    }

    @Test
    @DisplayName("Should return 200 OK with pre-filled profile when RUC lookup succeeds")
    void shouldReturn200WithPreFilledData() {
        String ruc = "20100047218";
        var resource = new CorporateLookupResource(
                ruc,
                "FINANCIAL_INSTITUTION",
                "ROLE_FINANCIAL_INSTITUTION",
                "BANCO DE CREDITO DEL PERU",
                "CALLE CENTENARIO 156",
                "150101",
                List.of("viabcp.com", "bcp.com.pe"),
                "https://logo.png",
                true
        );

        when(queryService.lookupByRuc(ruc)).thenReturn(Optional.of(resource));

        ResponseEntity<CorporateLookupResource> response = controller.lookupByRuc(ruc);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("BANCO DE CREDITO DEL PERU", response.getBody().suggestedName());
        assertEquals("ROLE_FINANCIAL_INSTITUTION", response.getBody().targetRole());
        assertTrue(response.getBody().allowedEmailDomains().contains("viabcp.com"));
    }

    @Test
    @DisplayName("Should return 404 Not Found when RUC does not exist in SUNAT")
    void shouldReturn404WhenNotFound() {
        String ruc = "20000000001";
        when(queryService.lookupByRuc(ruc)).thenReturn(Optional.empty());

        ResponseEntity<CorporateLookupResource> response = controller.lookupByRuc(ruc);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }
}
