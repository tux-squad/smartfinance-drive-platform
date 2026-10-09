package com.smartfinance.smartfinancedriveplatform.partners.infrastructure.tokens.sunat;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.smartfinance.smartfinancedriveplatform.partners.application.outboundservices.SunatRucVerifierService;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.valueobjects.SunatRucInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.Optional;

/**
 * Implementation of {@link SunatRucVerifierService} invoking Chequea.pe or Factiliza REST APIs with timeouts and caching.
 */
@Service
public class ChequeaRucVerifierServiceImpl implements SunatRucVerifierService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChequeaRucVerifierServiceImpl.class);

    private final RestClient chequeaRestClient;
    private final RestClient factilizaRestClient;

    @org.springframework.beans.factory.annotation.Autowired
    public ChequeaRucVerifierServiceImpl(
            @Value("${chequea.base-url:https://api.chequea.pe}") String chequeaBaseUrl,
            @Value("${chequea.api-key:}") String chequeaApiKey,
            @Value("${factiliza.base-url:https://api.factiliza.com}") String factilizaBaseUrl,
            @Value("${factiliza.api-key:}") String factilizaApiKey) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(3000);
        requestFactory.setReadTimeout(5000);

        this.chequeaRestClient = RestClient.builder()
                .requestFactory(requestFactory)
                .baseUrl(chequeaBaseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + chequeaApiKey)
                .defaultHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .defaultHeader(HttpHeaders.ACCEPT, "application/json")
                .build();

        this.factilizaRestClient = RestClient.builder()
                .requestFactory(requestFactory)
                .baseUrl(factilizaBaseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + factilizaApiKey)
                .defaultHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .defaultHeader(HttpHeaders.ACCEPT, "application/json")
                .build();
    }

    // Constructor for testing with base url and api key
    public ChequeaRucVerifierServiceImpl(String baseUrl, String apiKey) {
        this(baseUrl, apiKey, "https://api.factiliza.com", "");
    }

    // Constructor for testing with custom RestClient
    public ChequeaRucVerifierServiceImpl(RestClient restClient) {
        this.chequeaRestClient = restClient;
        this.factilizaRestClient = restClient;
    }

    @Override
    @Cacheable(value = "sunatRucCache", key = "#ruc", unless = "#result == null")
    public Optional<SunatRucInfo> verifyRuc(String ruc) {
        if (ruc == null || !ruc.matches("\\d{11}")) {
            LOGGER.warn("Invalid RUC format supplied: {}", ruc);
            return Optional.empty();
        }

        // Try Chequea.pe first
        try {
            var response = chequeaRestClient.get()
                    .uri("/api/v1/ruc/{ruc}", ruc)
                    .retrieve()
                    .body(ChequeaRucResponse.class);

            if (response != null && response.ruc() != null) {
                return Optional.of(new SunatRucInfo(
                        response.ruc(),
                        response.razonSocial(),
                        response.estado(),
                        response.condicion(),
                        response.tipo(),
                        response.ubigeo(),
                        response.direccion(),
                        response.ciiu()
                ));
            }
        } catch (HttpClientErrorException.NotFound e) {
            LOGGER.info("RUC {} not found in Chequea.pe database", ruc);
        } catch (Exception e) {
            LOGGER.warn("Chequea.pe query failed for RUC {}: {}. Attempting Factiliza fallback...", ruc, e.getMessage());
        }

        // Fallback to Factiliza API
        try {
            var response = factilizaRestClient.get()
                    .uri("/v1/ruc/info/{ruc}", ruc)
                    .retrieve()
                    .body(FactilizaRucResponse.class);

            if (response != null && Boolean.TRUE.equals(response.success()) && response.data() != null) {
                var data = response.data();
                return Optional.of(new SunatRucInfo(
                        data.numero(),
                        data.nombreORazonSocial(),
                        data.estado(),
                        data.condicion(),
                        data.tipoContribuyente(),
                        data.ubigeoSunat(),
                        data.direccionCompleta(),
                        null
                ));
            }
        } catch (HttpClientErrorException.NotFound e) {
            LOGGER.info("RUC {} not found in Factiliza database", ruc);
        } catch (Exception e) {
            LOGGER.error("Factiliza fallback failed for RUC {}: {}", ruc, e.getMessage());
        }

        return Optional.empty();
    }

    private record ChequeaRucResponse(
            String ruc,
            String razonSocial,
            String estado,
            String condicion,
            String tipo,
            String ubigeo,
            String direccion,
            String ciiu,
            String actualizado
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record FactilizaRucResponse(
            Integer status,
            String message,
            Boolean success,
            FactilizaRucData data
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record FactilizaRucData(
            String numero,
            @JsonProperty("nombre_o_razon_social") String nombreORazonSocial,
            @JsonProperty("tipo_contribuyente") String tipoContribuyente,
            String estado,
            String condicion,
            String departamento,
            String provincia,
            String distrito,
            String direccion,
            @JsonProperty("direccion_completa") String direccionCompleta,
            @JsonProperty("ubigeo_sunat") String ubigeoSunat
    ) {}
}

