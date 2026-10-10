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
        if (ciiu != null && !ciiu.isBlank()) {
            // CIIU 45 covers wholesale/retail and repair of motor vehicles & parts (4510, 4520, 4530, 4540)
            return ciiu.startsWith("45");
        }
        // Fallback: when provider does not include CIIU (e.g. Factiliza), infer from trade name
        if (razonSocial != null) {
            String upper = razonSocial.toUpperCase();
            return upper.contains("AUTOMOTRIZ") || upper.contains("MOTORS") || upper.contains("AUTOS") ||
                   upper.contains("CONCESIONARI") || upper.contains("VEHICUL") || upper.contains("AUTOMOTOR") ||
                   upper.contains("MITSUI") || upper.contains("TOYOTA") || upper.contains("NISSAN") ||
                   upper.contains("HYUNDAI") || upper.contains("DERCO") || upper.contains("DIVEMOTOR") ||
                   upper.contains("AUTOLAND") || upper.contains("BRAILLARD") || upper.contains("EUROMOTORS");
        }
        return false;
    }

    public boolean isFinancialInstitutionCiiu() {
        if (ciiu == null || ciiu.isBlank()) {
            return false;
        }
        // CIIU 64xx and 66xx represent financial intermediation & auxiliary financial activities
        return ciiu.startsWith("64") || ciiu.startsWith("66");
    }
}
