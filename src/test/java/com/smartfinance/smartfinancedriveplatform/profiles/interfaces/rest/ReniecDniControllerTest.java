package com.smartfinance.smartfinancedriveplatform.profiles.interfaces.rest;

import com.smartfinance.smartfinancedriveplatform.profiles.application.outboundservices.ReniecDniVerifierService;
import com.smartfinance.smartfinancedriveplatform.profiles.domain.model.valueobjects.ReniecDniInfo;
import com.smartfinance.smartfinancedriveplatform.profiles.interfaces.rest.resources.ReniecDniResource;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReniecDniController Unit Tests")
class ReniecDniControllerTest {

    @Mock
    private ReniecDniVerifierService reniecDniVerifierService;

    @InjectMocks
    private ReniecDniController reniecDniController;

    @Test
    @DisplayName("Should return 200 OK on getDniInfo when valid DNI found in RENIEC")
    void shouldReturnOkOnValidDni() {
        ReniecDniInfo info = new ReniecDniInfo(
                "27427864",
                "7",
                "JOSE PEDRO",
                "CASTILLO",
                "TERRONES",
                "CASTILLO TERRONES, JOSE PEDRO",
                "CAJAMARCA",
                "CHOTA",
                "TACABAMBA",
                "CASERIO PUÑA",
                "CASERIO PUÑA, CAJAMARCA - CHOTA - TACABAMBA",
                "060615",
                "060417",
                List.of("06", "0604", "060417"),
                "1969-10-19",
                "CASADO",
                null,
                "M"
        );

        when(reniecDniVerifierService.verifyDni("27427864")).thenReturn(Optional.of(info));

        ResponseEntity<ReniecDniResource> response = reniecDniController.getDniInfo("27427864");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("27427864", response.getBody().dni());
        assertEquals("JOSE PEDRO", response.getBody().firstNames());
        assertEquals("CASTILLO", response.getBody().paternalSurname());
        assertEquals("TERRONES", response.getBody().maternalSurname());
        assertEquals("CASTILLO TERRONES, JOSE PEDRO", response.getBody().fullLegalName());
        assertEquals("CAJAMARCA", response.getBody().department());
        assertEquals("CHOTA", response.getBody().province());
        assertEquals("TACABAMBA", response.getBody().district());
        assertEquals("CASERIO PUÑA, CAJAMARCA - CHOTA - TACABAMBA", response.getBody().fullAddress());
        assertEquals("060615", response.getBody().ubigeoReniec());
        assertEquals("060417", response.getBody().ubigeoSunat());
    }

    @Test
    @DisplayName("Should return 404 Not Found when DNI does not exist")
    void shouldReturnNotFoundWhenDniDoesNotExist() {
        when(reniecDniVerifierService.verifyDni("99999999")).thenReturn(Optional.empty());

        ResponseEntity<ReniecDniResource> response = reniecDniController.getDniInfo("99999999");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    @DisplayName("Should validate DNI parameter format using @Pattern constraint")
    void shouldRejectInvalidDniFormat() throws NoSuchMethodException {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        var method = ReniecDniController.class.getMethod("getDniInfo", String.class);

        var violationsShort = validator.forExecutables().validateParameters(
                reniecDniController, method, new Object[]{"1234"}
        );
        var violationsAlpha = validator.forExecutables().validateParameters(
                reniecDniController, method, new Object[]{"ABCDEFGH"}
        );
        var violationsValid = validator.forExecutables().validateParameters(
                reniecDniController, method, new Object[]{"27427864"}
        );

        assertFalse(violationsShort.isEmpty());
        assertFalse(violationsAlpha.isEmpty());
        assertTrue(violationsValid.isEmpty());
    }
}
