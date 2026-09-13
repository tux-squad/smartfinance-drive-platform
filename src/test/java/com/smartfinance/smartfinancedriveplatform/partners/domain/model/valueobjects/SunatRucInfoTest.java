package com.smartfinance.smartfinancedriveplatform.partners.domain.model.valueobjects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SunatRucInfo Value Object Unit Tests")
class SunatRucInfoTest {

    @Test
    @DisplayName("Should validate isActiveAndHabido correctly")
    void shouldValidateIsActiveAndHabido() {
        SunatRucInfo activeInfo = new SunatRucInfo("20100047218", "BANCO CONTINENTAL", "ACTIVO", "HABIDO", "SOCIEDAD ANONIMA", "150131", "AV. REPUBLICA DE PANAMA 3055", "6419");
        assertTrue(activeInfo.isActiveAndHabido());

        SunatRucInfo inactiveInfo = new SunatRucInfo("20100047218", "BANCO CONTINENTAL", "BAJA DEFINITIVA", "HABIDO", "SOCIEDAD ANONIMA", "150131", "AV. REPUBLICA DE PANAMA 3055", "6419");
        assertFalse(inactiveInfo.isActiveAndHabido());
    }

    @Test
    @DisplayName("Should identify automotive CIIU correctly")
    void shouldIdentifyAutomotiveCiiu() {
        SunatRucInfo automotiveInfo = new SunatRucInfo("20100128056", "TOYOTA DEL PERU S.A.", "ACTIVO", "HABIDO", "SOCIEDAD ANONIMA", "150101", "AV. SANTO TORIBIO 173", "4510");
        assertTrue(automotiveInfo.isAutomotiveCiiu());
        assertFalse(automotiveInfo.isFinancialInstitutionCiiu());
    }

    @Test
    @DisplayName("Should identify financial institution CIIUs (64xx, 66xx) correctly")
    void shouldIdentifyFinancialInstitutionCiiu() {
        SunatRucInfo bankInfo = new SunatRucInfo("20100047218", "BANCO CONTINENTAL", "ACTIVO", "HABIDO", "SOCIEDAD ANONIMA", "150131", "AV. REPUBLICA DE PANAMA 3055", "6419");
        assertTrue(bankInfo.isFinancialInstitutionCiiu());
        assertFalse(bankInfo.isAutomotiveCiiu());

        SunatRucInfo financeInfo = new SunatRucInfo("20500000000", "FINANCIERA X", "ACTIVO", "HABIDO", "SOCIEDAD ANONIMA", "150101", "AV. LIMA 123", "6430");
        assertTrue(financeInfo.isFinancialInstitutionCiiu());

        SunatRucInfo auxiliaryInfo = new SunatRucInfo("20600000000", "CORRETAJE Y", "ACTIVO", "HABIDO", "SOCIEDAD ANONIMA", "150101", "AV. LIMA 456", "6612");
        assertTrue(auxiliaryInfo.isFinancialInstitutionCiiu());
    }
}
