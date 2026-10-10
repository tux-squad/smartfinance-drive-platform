package com.smartfinance.smartfinancedriveplatform.partners.domain.services;

import com.smartfinance.smartfinancedriveplatform.partners.domain.model.valueobjects.CorporateEntityType;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.valueobjects.SunatRucInfo;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Domain catalog and registry of authorized corporate email domains and entity classifications
 * for Peruvian financial institutions and automotive dealerships.
 */
@Component
public class CorporateDomainCatalog {

    private static final Map<String, List<String>> RUC_TO_DOMAINS = new LinkedHashMap<>();
    private static final Map<String, CorporateEntityType> RUC_TO_TYPE = new LinkedHashMap<>();

    static {
        // --- Financial Entities (Bancos e Instituciones Financieras) ---
        register("20100047218", CorporateEntityType.FINANCIAL_INSTITUTION, "viabcp.com", "bcp.com.pe");
        register("20100130204", CorporateEntityType.FINANCIAL_INSTITUTION, "bbva.com", "bbva.pe");
        register("20100053455", CorporateEntityType.FINANCIAL_INSTITUTION, "interbank.pe", "interbank.com.pe");
        register("20100010561", CorporateEntityType.FINANCIAL_INSTITUTION, "scotiabank.com.pe");
        register("20100105862", CorporateEntityType.FINANCIAL_INSTITUTION, "banbif.com.pe");
        register("20100105860", CorporateEntityType.FINANCIAL_INSTITUTION, "pichincha.pe");
        register("20100138281", CorporateEntityType.FINANCIAL_INSTITUTION, "santander.com.pe");
        register("20100070970", CorporateEntityType.FINANCIAL_INSTITUTION, "mibanco.com.pe");
        register("20100105943", CorporateEntityType.FINANCIAL_INSTITUTION, "bancofalabella.pe");
        register("20100084334", CorporateEntityType.FINANCIAL_INSTITUTION, "bancoripley.com.pe");
        register("20100105871", CorporateEntityType.FINANCIAL_INSTITUTION, "bancom.pe");
        register("20100047226", CorporateEntityType.FINANCIAL_INSTITUTION, "grupognb.com");

        // --- Dealerships (Concesionarias de Vehículos) ---
        register("20349887714", CorporateEntityType.DEALERSHIP, "autoland.com.pe");
        register("20100035121", CorporateEntityType.DEALERSHIP, "derco.com.pe");
        register("20100111161", CorporateEntityType.DEALERSHIP, "divemotor.com");
        register("20100041287", CorporateEntityType.DEALERSHIP, "braillard.com.pe");
        register("20100008400", CorporateEntityType.DEALERSHIP, "euromotors.com.pe");
        register("20100078016", CorporateEntityType.DEALERSHIP, "grupopana.com.pe");
        register("20100174091", CorporateEntityType.DEALERSHIP, "limautos.pe");
        register("20506308151", CorporateEntityType.DEALERSHIP, "mitsuiautomotriz.com");
        register("20256211310", CorporateEntityType.DEALERSHIP, "mitsuiautomotriz.com");
        register("20512686811", CorporateEntityType.DEALERSHIP, "wigo.pe");
    }

    private static void register(String ruc, CorporateEntityType type, String... domains) {
        RUC_TO_DOMAINS.put(ruc, Arrays.asList(domains));
        RUC_TO_TYPE.put(ruc, type);
    }

    /**
     * Resolves the corporate entity type based on explicit catalog registry or SUNAT CIIU codes.
     */
    public CorporateEntityType determineEntityType(SunatRucInfo rucInfo) {
        if (rucInfo == null) {
            return CorporateEntityType.UNKNOWN;
        }

        String ruc = rucInfo.ruc();
        if (ruc != null && RUC_TO_TYPE.containsKey(ruc.trim())) {
            return RUC_TO_TYPE.get(ruc.trim());
        }

        if (rucInfo.isFinancialInstitutionCiiu()) {
            return CorporateEntityType.FINANCIAL_INSTITUTION;
        }

        if (rucInfo.isAutomotiveCiiu()) {
            return CorporateEntityType.DEALERSHIP;
        }

        return CorporateEntityType.UNKNOWN;
    }

    /**
     * Returns known authorized corporate email domains for a given RUC.
     */
    public List<String> getAuthorizedDomains(String ruc) {
        if (ruc == null) {
            return Collections.emptyList();
        }
        return RUC_TO_DOMAINS.getOrDefault(ruc.trim(), Collections.emptyList());
    }

    /**
     * Checks if a corporate email matches one of the authorized domains for the given RUC.
     */
    public boolean isDomainAllowedForRuc(String ruc, String email, Set<String> customEntityDomains) {
        if (email == null || !email.contains("@")) {
            return false;
        }

        String domain = email.substring(email.lastIndexOf("@") + 1).trim().toLowerCase();

        // 1. Check custom domains associated directly with the entity in DB
        if (customEntityDomains != null && !customEntityDomains.isEmpty()) {
            for (String custom : customEntityDomains) {
                if (custom != null && custom.trim().equalsIgnoreCase(domain)) {
                    return true;
                }
            }
        }

        // 2. Check catalog seed domains
        List<String> catalogDomains = getAuthorizedDomains(ruc);
        for (String catalogDomain : catalogDomains) {
            if (catalogDomain.equalsIgnoreCase(domain)) {
                return true;
            }
        }

        return false;
    }
}
