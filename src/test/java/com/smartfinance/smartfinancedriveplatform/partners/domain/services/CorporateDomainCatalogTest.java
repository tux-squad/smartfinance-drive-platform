package com.smartfinance.smartfinancedriveplatform.partners.domain.services;

import com.smartfinance.smartfinancedriveplatform.partners.domain.model.valueobjects.CorporateEntityType;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.valueobjects.SunatRucInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CorporateDomainCatalog Unit Tests")
class CorporateDomainCatalogTest {

    private CorporateDomainCatalog catalog;

    @BeforeEach
    void setUp() {
        catalog = new CorporateDomainCatalog();
    }

    @Test
    @DisplayName("Should recognize BCP bank RUC and return official allowed domains")
    void shouldRecognizeBcpRucAndReturnAllowedDomains() {
        var rucInfo = new SunatRucInfo("20100047218", "BANCO DE CREDITO DEL PERU", "ACTIVO", "HABIDO", "SOCIEDAD ANONIMA ABIERTA", "150101", "CAL. CENTENARIO 156", "6419");
        CorporateEntityType type = catalog.determineEntityType(rucInfo);
        assertEquals(CorporateEntityType.FINANCIAL_INSTITUTION, type);

        var domains = catalog.getAuthorizedDomains("20100047218");
        assertTrue(domains.contains("viabcp.com"));
        assertTrue(domains.contains("bcp.com.pe"));

        assertTrue(catalog.isDomainAllowedForRuc("20100047218", "analista@viabcp.com", null));
        assertTrue(catalog.isDomainAllowedForRuc("20100047218", "funcionario@bcp.com.pe", null));
        assertFalse(catalog.isDomainAllowedForRuc("20100047218", "impostor@gmail.com", null));
    }

    @Test
    @DisplayName("Should recognize Autoland dealership RUC and return allowed domains")
    void shouldRecognizeAutolandDealershipAndReturnAllowedDomains() {
        var rucInfo = new SunatRucInfo("20349887714", "AUTOLAND S.A.", "ACTIVO", "HABIDO", "SOCIEDAD ANONIMA", "150101", "AV. JAVIER PRADO ESTE 5020", "4510");
        CorporateEntityType type = catalog.determineEntityType(rucInfo);
        assertEquals(CorporateEntityType.DEALERSHIP, type);

        var domains = catalog.getAuthorizedDomains("20349887714");
        assertTrue(domains.contains("autoland.com.pe"));

        assertTrue(catalog.isDomainAllowedForRuc("20349887714", "gerente@autoland.com.pe", null));
        assertFalse(catalog.isDomainAllowedForRuc("20349887714", "gerente@hotmail.com", null));
    }

    @Test
    @DisplayName("Should infer entity type from CIIU code if not registered in static seeds")
    void shouldInferEntityTypeFromCiiuCode() {
        var bankInfo = new SunatRucInfo("20999999999", "NUEVA CAJA FINANCIERA", "ACTIVO", "HABIDO", "S.A.", "150101", "DIR", "6499");
        assertEquals(CorporateEntityType.FINANCIAL_INSTITUTION, catalog.determineEntityType(bankInfo));

        var dealerInfo = new SunatRucInfo("20888888888", "NUEVO DEALER MOTORS", "ACTIVO", "HABIDO", "S.A.", "150101", "DIR", "4510");
        assertEquals(CorporateEntityType.DEALERSHIP, catalog.determineEntityType(dealerInfo));

        var bakeryInfo = new SunatRucInfo("20777777777", "PANADERIA CENTRAL", "ACTIVO", "HABIDO", "S.A.", "150101", "DIR", "1071");
        assertEquals(CorporateEntityType.UNKNOWN, catalog.determineEntityType(bakeryInfo));
    }

    @Test
    @DisplayName("Should support custom entity domains passed from entity repository")
    void shouldSupportCustomEntityDomains() {
        boolean allowed = catalog.isDomainAllowedForRuc(
                "20888888888",
                "contacto@nuevodealer.com.pe",
                Set.of("nuevodealer.com.pe")
        );
        assertTrue(allowed);
    }

    @Test
    @DisplayName("Should strictly reject email domain spoofing, suffix extensions, and evil subdomains")
    void shouldRejectDomainSpoofingAttempts() {
        String bcpRuc = "20100047218";
        // Subdomain of evil domain:
        assertFalse(catalog.isDomainAllowedForRuc(bcpRuc, "attacker@viabcp.com.evil.com", null));
        // Suffix/prefix typo-squatting:
        assertFalse(catalog.isDomainAllowedForRuc(bcpRuc, "attacker@fakeviabcp.com", null));
        assertFalse(catalog.isDomainAllowedForRuc(bcpRuc, "attacker@viabcp.pe", null));
        assertFalse(catalog.isDomainAllowedForRuc(bcpRuc, "attacker@notviabcp.com", null));
        // Multiple @ or invalid emails:
        assertFalse(catalog.isDomainAllowedForRuc(bcpRuc, "attacker@viabcp.com@evil.com", null));
        assertFalse(catalog.isDomainAllowedForRuc(bcpRuc, "invalid-email-without-at", null));
        assertFalse(catalog.isDomainAllowedForRuc(bcpRuc, "", null));
        assertFalse(catalog.isDomainAllowedForRuc(bcpRuc, null, null));

        String autolandRuc = "20349887714";
        assertFalse(catalog.isDomainAllowedForRuc(autolandRuc, "attacker@autoland.com.pe.attacker.com", null));
        assertFalse(catalog.isDomainAllowedForRuc(autolandRuc, "attacker@evilautoland.com.pe", null));
    }

    @Test
    @DisplayName("Should return UNKNOWN entity type when entity is not in catalog and has null or unrelated CIIU")
    void shouldReturnUnknownWhenNoCatalogAndNoCiiu() {
        var unknownInfoNullCiiu = new SunatRucInfo("20999999991", "EMPRESA SIN CIIU", "ACTIVO", "HABIDO", "S.A.", "150101", "DIR", null);
        assertEquals(CorporateEntityType.UNKNOWN, catalog.determineEntityType(unknownInfoNullCiiu));

        var unknownInfoBlankCiiu = new SunatRucInfo("20999999992", "EMPRESA BLANK CIIU", "ACTIVO", "HABIDO", "S.A.", "150101", "DIR", "  ");
        assertEquals(CorporateEntityType.UNKNOWN, catalog.determineEntityType(unknownInfoBlankCiiu));
    }
}
