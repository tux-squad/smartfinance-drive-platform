package com.smartfinance.smartfinancedriveplatform.partners.interfaces.rest.resources;

/**
 * REST response resource representation for SUNAT RUC query details.
 */
public record SunatRucResource(
        String ruc,
        String razonSocial,
        String estado,
        String condicion,
        String tipo,
        String ubigeo,
        String direccion,
        String ciiu,
        boolean isActiveAndHabido,
        boolean isAutomotiveCiiu
) {}
