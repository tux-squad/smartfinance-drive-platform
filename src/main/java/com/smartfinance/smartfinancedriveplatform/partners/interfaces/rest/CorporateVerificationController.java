package com.smartfinance.smartfinancedriveplatform.partners.interfaces.rest;

import com.smartfinance.smartfinancedriveplatform.partners.application.queryservices.CorporateLookupQueryService;
import com.smartfinance.smartfinancedriveplatform.partners.interfaces.rest.resources.CorporateLookupResource;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for official B2B corporate verification lookup and profile pre-filling.
 */
@RestController
@RequestMapping(value = "/api/v1/partners/corporate-verification", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
public class CorporateVerificationController {

    private final CorporateLookupQueryService corporateLookupQueryService;

    public CorporateVerificationController(CorporateLookupQueryService corporateLookupQueryService) {
        this.corporateLookupQueryService = corporateLookupQueryService;
    }

    /**
     * GET /api/v1/partners/corporate-verification/lookup/{ruc}
     * Queries SUNAT to pre-fill profile data and authorized corporate email domains for Banks and Dealerships.
     *
     * @param ruc The 11-digit Peruvian RUC string.
     * @return The pre-filled corporate lookup resource payload.
     */
    @GetMapping("/lookup/{ruc}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CorporateLookupResource> lookupByRuc(
            @PathVariable
            @Pattern(regexp = "^\\d{11}$", message = "RUC must consist of exactly 11 numeric digits")
            String ruc) {
        var resourceOpt = corporateLookupQueryService.lookupByRuc(ruc);
        return resourceOpt
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
