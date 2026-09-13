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
            return false;
        }
        // CIIU 4510 / 45100 represents sale of motor vehicles
        return ciiu.startsWith("451") || ciiu.startsWith("4510");
    }
}
