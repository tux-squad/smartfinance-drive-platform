package com.smartfinance.smartfinancedriveplatform.partners.infrastructure.tokens.sunat;

import com.smartfinance.smartfinancedriveplatform.partners.application.outboundservices.SunatRucVerifierService;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.valueobjects.SunatRucInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Optional;

/**
 * Implementation of {@link SunatRucVerifierService} invoking the Chequea.pe REST API.
 */
@Service
public class ChequeaRucVerifierServiceImpl implements SunatRucVerifierService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChequeaRucVerifierServiceImpl.class);

    private final RestClient restClient;

    public ChequeaRucVerifierServiceImpl(
            @Value("${chequea.base-url:https://api.chequea.pe}") String baseUrl,
            @Value("${chequea.api-key:ak_live_KVZ4-T3DFFPl5WzMckwyet54TnzhTHV_q6GOj10bntE}") String apiKey) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .build();
    }

    // Constructor for testing with custom RestClient
    public ChequeaRucVerifierServiceImpl(RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
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
        } catch (Exception e) {
            LOGGER.error("Failed to query Chequea.pe API for RUC {}: {}", ruc, e.getMessage());
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
