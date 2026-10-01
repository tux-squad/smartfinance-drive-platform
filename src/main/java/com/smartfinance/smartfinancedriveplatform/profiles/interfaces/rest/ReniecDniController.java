package com.smartfinance.smartfinancedriveplatform.profiles.interfaces.rest;

import com.smartfinance.smartfinancedriveplatform.profiles.application.outboundservices.ReniecDniVerifierService;
import com.smartfinance.smartfinancedriveplatform.profiles.interfaces.rest.resources.ReniecDniResource;
import com.smartfinance.smartfinancedriveplatform.profiles.interfaces.rest.transform.ReniecDniResourceFromInfoAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
 * REST Controller for querying citizen identification data from RENIEC / Factiliza.
 * Used by frontend to automatically populate customer profile fields during onboarding and profile setup.
 */
@RestController
@RequestMapping(value = "/api/v1/profiles/reniec", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
@Tag(name = "Profiles", description = "Customer Profile Management & Identity Verification")
public class ReniecDniController {

    private final ReniecDniVerifierService reniecDniVerifierService;

    public ReniecDniController(ReniecDniVerifierService reniecDniVerifierService) {
        this.reniecDniVerifierService = reniecDniVerifierService;
    }

    /**
     * GET /api/v1/profiles/reniec/dni/{dni}
     * Queries official RENIEC information by DNI for profile auto-filling.
     *
     * @param dni The 8-digit Peruvian National ID string.
     * @return The citizen identity resource payload.
     */
    @GetMapping("/dni/{dni}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Query RENIEC identity by DNI", description = "Retrieves citizen identity details for auto-filling customer profile forms.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "DNI successfully found and retrieved"),
            @ApiResponse(responseCode = "400", description = "Invalid DNI format (must be 8 digits)"),
            @ApiResponse(responseCode = "404", description = "DNI not found in RENIEC database"),
            @ApiResponse(responseCode = "429", description = "Rate limit exceeded (max 10 requests per minute)")
    })
    public ResponseEntity<ReniecDniResource> getDniInfo(
            @PathVariable
            @Pattern(regexp = "^\\d{8}$", message = "DNI must consist of exactly 8 numeric digits")
            String dni) {
        var dniInfoOpt = reniecDniVerifierService.verifyDni(dni);
        return dniInfoOpt
                .map(info -> ResponseEntity.ok(ReniecDniResourceFromInfoAssembler.toResourceFromInfo(info)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
