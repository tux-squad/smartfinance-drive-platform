package com.smartfinance.smartfinancedriveplatform.partners.infrastructure.tokens.sunat;

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
 * Implementation of {@link SunatRucVerifierService} invoking the Chequea.pe REST API with timeouts and caching.
 */
@Service
public class ChequeaRucVerifierServiceImpl implements SunatRucVerifierService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChequeaRucVerifierServiceImpl.class);

    private final RestClient restClient;

    @org.springframework.beans.factory.annotation.Autowired
    public ChequeaRucVerifierServiceImpl(
            @Value("${chequea.base-url}") String baseUrl,
            @Value("${chequea.api-key}") String apiKey) {
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

    // Constructor for testing with custom RestClient
    public ChequeaRucVerifierServiceImpl(RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    @Cacheable(value = "sunatRucCache", key = "#ruc", unless = "#result == null")
    public Optional<SunatRucInfo> verifyRuc(String ruc) {
        if (ruc == null || !ruc.matches("\\d{11}")) {
            LOGGER.warn("Invalid RUC format supplied: {}", ruc);
            return Optional.empty();
        }

        try {
            var response = restClient.get()
                    .uri("/api/v1/ruc/{ruc}", ruc)
                    .retrieve()
                    .body(ChequeaRucResponse.class);

            if (response == null || response.ruc() == null) {
                return Optional.empty();
            }

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
        } catch (HttpClientErrorException.NotFound e) {
            LOGGER.info("RUC {} not found in SUNAT database", ruc);
            return Optional.empty();
        } catch (HttpClientErrorException e) {
            LOGGER.warn("Client error when querying Chequea.pe for RUC {}: Status {}", ruc, e.getStatusCode());
            return Optional.empty();
        } catch (Exception e) {
            LOGGER.error("External SUNAT API service failure for RUC {}: {}", ruc, e.getMessage());
            return Optional.empty();
        }
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
}
