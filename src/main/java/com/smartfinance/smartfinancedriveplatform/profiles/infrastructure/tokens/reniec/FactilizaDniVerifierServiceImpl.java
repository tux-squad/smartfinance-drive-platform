package com.smartfinance.smartfinancedriveplatform.profiles.infrastructure.tokens.reniec;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.smartfinance.smartfinancedriveplatform.profiles.application.outboundservices.ReniecDniVerifierService;
import com.smartfinance.smartfinancedriveplatform.profiles.domain.model.valueobjects.ReniecDniInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of {@link ReniecDniVerifierService} invoking the Factiliza REST API
 * with connection timeouts, caching, and resilient error handling.
 */
@Service
public class FactilizaDniVerifierServiceImpl implements ReniecDniVerifierService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FactilizaDniVerifierServiceImpl.class);

    private final RestClient restClient;

    @Autowired
    public FactilizaDniVerifierServiceImpl(
            @Value("${factiliza.base-url}") String baseUrl,
            @Value("${factiliza.api-key}") String apiKey) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(3000);
        requestFactory.setReadTimeout(5000);

        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .defaultHeader(HttpHeaders.ACCEPT, "application/json")
                .build();
    }

    // Constructor for testing with a custom or mocked RestClient
    public FactilizaDniVerifierServiceImpl(RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    @Cacheable(value = "reniecDniCache", key = "#dni", unless = "#result == null")
    public Optional<ReniecDniInfo> verifyDni(String dni) {
        if (dni == null || !dni.matches("\\d{8}")) {
            LOGGER.warn("Invalid DNI format supplied: {}", dni);
            return Optional.empty();
        }

        try {
            var response = restClient.get()
                    .uri("/v1/dni/info/{dni}", dni)
                    .retrieve()
                    .body(FactilizaDniResponse.class);

            if (response == null || !Boolean.TRUE.equals(response.success()) || response.data() == null) {
                return Optional.empty();
            }

            var data = response.data();
            return Optional.of(new ReniecDniInfo(
                    data.numero(),
                    data.codigoVerificacion(),
                    data.nombres(),
                    data.apellidoPaterno(),
                    data.apellidoMaterno(),
                    data.nombreCompleto(),
                    data.departamento(),
                    data.provincia(),
                    data.distrito(),
                    data.direccion(),
                    data.direccionCompleta(),
                    data.ubigeoReniec(),
                    data.ubigeoSunat(),
                    data.ubigeo(),
                    data.fechaNacimiento(),
                    data.estadoCivil(),
                    data.foto(),
                    data.sexo()
            ));
        } catch (HttpClientErrorException.NotFound e) {
            LOGGER.info("DNI {} not found in RENIEC/Factiliza database", dni);
            return Optional.empty();
        } catch (HttpClientErrorException e) {
            LOGGER.warn("Client error when querying Factiliza for DNI {}: Status {}", dni, e.getStatusCode());
            return Optional.empty();
        } catch (Exception e) {
            LOGGER.error("External Factiliza API service failure for DNI {}: {}", dni, e.getMessage());
            return Optional.empty();
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record FactilizaDniResponse(
            Integer status,
            String message,
            Boolean success,
            FactilizaDniData data,
            Integer fuente
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record FactilizaDniData(
            String numero,
            @JsonProperty("codigo_verificacion") String codigoVerificacion,
            String nombres,
            @JsonProperty("apellido_paterno") String apellidoPaterno,
            @JsonProperty("apellido_materno") String apellidoMaterno,
            @JsonProperty("nombre_completo") String nombreCompleto,
            String departamento,
            String provincia,
            String distrito,
            String direccion,
            @JsonProperty("direccion_completa") String direccionCompleta,
            @JsonProperty("ubigeo_reniec") String ubigeoReniec,
            @JsonProperty("ubigeo_sunat") String ubigeoSunat,
            List<String> ubigeo,
            @JsonProperty("fecha_nacimiento") String fechaNacimiento,
            @JsonProperty("estado_civil") String estadoCivil,
            String foto,
            String sexo
    ) {}
}
