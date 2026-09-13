package com.smartfinance.smartfinancedriveplatform.partners.interfaces.rest;

import com.smartfinance.smartfinancedriveplatform.partners.application.outboundservices.SunatRucVerifierService;
import com.smartfinance.smartfinancedriveplatform.partners.interfaces.rest.resources.SunatRucResource;
import com.smartfinance.smartfinancedriveplatform.partners.interfaces.rest.transform.SunatRucResourceFromInfoAssembler;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for querying official SUNAT RUC details.
 */
@RestController
@RequestMapping(value = "/api/v1/partners/sunat", produces = MediaType.APPLICATION_JSON_VALUE)
public class SunatRucController {

    private final SunatRucVerifierService sunatRucVerifierService;

    public SunatRucController(SunatRucVerifierService sunatRucVerifierService) {
        this.sunatRucVerifierService = sunatRucVerifierService;
    }

    /**
     * GET /api/v1/partners/sunat/ruc/{ruc}
     * Queries official SUNAT RUC information for dealership verification.
     *
     * @param ruc The 11-digit Peruvian RUC string.
     * @return The SUNAT RUC resource payload.
     */
    @GetMapping("/ruc/{ruc}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SunatRucResource> getRucInfo(@PathVariable String ruc) {
        var rucInfoOpt = sunatRucVerifierService.verifyRuc(ruc);
        return rucInfoOpt
                .map(info -> ResponseEntity.ok(SunatRucResourceFromInfoAssembler.toResourceFromInfo(info)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
