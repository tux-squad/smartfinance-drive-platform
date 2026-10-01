package com.smartfinance.smartfinancedriveplatform.profiles.infrastructure.tokens.reniec;

import com.smartfinance.smartfinancedriveplatform.profiles.domain.model.valueobjects.ReniecDniInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withResourceNotFound;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@DisplayName("FactilizaDniVerifierServiceImpl Unit Tests")
class FactilizaDniVerifierServiceImplTest {

    private MockRestServiceServer mockServer;
    private FactilizaDniVerifierServiceImpl service;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://api.factiliza.com");
        mockServer = MockRestServiceServer.bindTo(builder).build();
        service = new FactilizaDniVerifierServiceImpl(builder.build());
    }

    @Test
    @DisplayName("Should return empty Optional when invalid DNI length, non-numeric, or null format supplied")
    void shouldReturnEmptyWhenInvalidDniFormat() {
        FactilizaDniVerifierServiceImpl localService = new FactilizaDniVerifierServiceImpl("https://api.factiliza.com", "dummy-key");

        Optional<ReniecDniInfo> resultShort = localService.verifyDni("1234");
        Optional<ReniecDniInfo> resultLong = localService.verifyDni("123456789");
        Optional<ReniecDniInfo> resultAlpha = localService.verifyDni("ABCDEFGH");
        Optional<ReniecDniInfo> resultNull = localService.verifyDni(null);

        assertTrue(resultShort.isEmpty());
        assertTrue(resultLong.isEmpty());
        assertTrue(resultAlpha.isEmpty());
        assertTrue(resultNull.isEmpty());
    }

    @Test
    @DisplayName("Should successfully parse Factiliza JSON response into ReniecDniInfo")
    void shouldParseFactilizaResponseSuccessfully() {
        String jsonResponse = """
                {
                  "status": 200,
                  "message": "Exito",
                  "success": true,
                  "data": {
                    "numero": "27427864",
                    "codigo_verificacion": "7",
                    "nombres": "JOSE PEDRO",
                    "apellido_paterno": "CASTILLO",
                    "apellido_materno": "TERRONES",
                    "nombre_completo": "CASTILLO TERRONES, JOSE PEDRO",
                    "departamento": "CAJAMARCA",
                    "provincia": "CHOTA",
                    "distrito": "TACABAMBA",
                    "direccion": "CASERIO PUÑA",
                    "direccion_completa": "CASERIO PUÑA, CAJAMARCA - CHOTA - TACABAMBA",
                    "ubigeo_reniec": "060615",
                    "ubigeo_sunat": "060417",
                    "ubigeo": ["06", "0604", "060417"],
                    "fecha_nacimiento": "1969-10-19",
                    "estado_civil": "CASADO",
                    "foto": "",
                    "sexo": "M"
                  },
                  "fuente": 0
                }
                """;

        mockServer.expect(requestTo("https://api.factiliza.com/v1/dni/info/27427864"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        Optional<ReniecDniInfo> result = service.verifyDni("27427864");

        assertThat(result).isPresent();
        ReniecDniInfo info = result.get();
        assertThat(info.dni()).isEqualTo("27427864");
        assertThat(info.firstNames()).isEqualTo("JOSE PEDRO");
        assertThat(info.paternalSurname()).isEqualTo("CASTILLO");
        assertThat(info.maternalSurname()).isEqualTo("TERRONES");
        assertThat(info.fullLegalName()).isEqualTo("CASTILLO TERRONES, JOSE PEDRO");
        assertThat(info.department()).isEqualTo("CAJAMARCA");
        assertThat(info.district()).isEqualTo("TACABAMBA");
        assertThat(info.fullAddress()).isEqualTo("CASERIO PUÑA, CAJAMARCA - CHOTA - TACABAMBA");
        assertThat(info.ubigeoSunat()).isEqualTo("060417");
        mockServer.verify();
    }

    @Test
    @DisplayName("Should return empty Optional when Factiliza returns 404 Not Found")
    void shouldReturnEmptyWhenFactilizaReturnsNotFound() {
        mockServer.expect(requestTo("https://api.factiliza.com/v1/dni/info/99999999"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withResourceNotFound());

        Optional<ReniecDniInfo> result = service.verifyDni("99999999");

        assertThat(result).isEmpty();
        mockServer.verify();
    }

    @Test
    @DisplayName("Should return empty Optional when Factiliza returns 500 Internal Server Error")
    void shouldReturnEmptyWhenFactilizaFails() {
        mockServer.expect(requestTo("https://api.factiliza.com/v1/dni/info/27427864"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withServerError());

        Optional<ReniecDniInfo> result = service.verifyDni("27427864");

        assertThat(result).isEmpty();
        mockServer.verify();
    }
}
