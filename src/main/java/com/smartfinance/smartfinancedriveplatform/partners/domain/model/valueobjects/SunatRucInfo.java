package com.smartfinance.smartfinancedriveplatform.partners.domain.model.valueobjects;

/**
 * Value Object representing official SUNAT RUC information returned from verification services.
 */
public record SunatRucInfo(
        String ruc,
        String razonSocial,
        String estado,
        String condicion,
        String tipo,
        String ubigeo,
        String direccion,
        String ciiu
) {
    public boolean isActiveAndHabido() {
        return "ACTIVO".equalsIgnoreCase(estado) && "HABIDO".equalsIgnoreCase(condicion);
    }

    public boolean isAutomotiveCiiu() {
        if (ciiu == null || ciiu.isBlank()) {
            // Fallback: If SUNAT API does not publish CIIU, accept RUC if verified as active & habido
            return true;
        }
        // CIIU 451 / 4510 / 45100 represents sale of motor vehicles
        return ciiu.startsWith("451");
    }

    public boolean isFinancialInstitutionCiiu() {
        if (ciiu == null || ciiu.isBlank()) {
            // Fallback: If SUNAT API does not publish CIIU, accept RUC if verified as active & habido
            return true;
        }
        // CIIU 64xx and 66xx represent financial intermediation & auxiliary financial activities
        return ciiu.startsWith("64") || ciiu.startsWith("66");
    }
}
