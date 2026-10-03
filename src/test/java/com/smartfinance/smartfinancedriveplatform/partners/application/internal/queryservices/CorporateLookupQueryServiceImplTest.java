package com.smartfinance.smartfinancedriveplatform.partners.application.internal.queryservices;

import com.smartfinance.smartfinancedriveplatform.partners.application.outboundservices.SunatRucVerifierService;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.aggregates.Dealership;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.aggregates.FinancialEntity;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.valueobjects.CorporateEntityType;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.valueobjects.FinancialEntityId;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.valueobjects.SunatRucInfo;
import com.smartfinance.smartfinancedriveplatform.partners.domain.repositories.DealershipRepository;
import com.smartfinance.smartfinancedriveplatform.partners.domain.repositories.FinancialEntityRepository;
import com.smartfinance.smartfinancedriveplatform.partners.domain.services.CorporateDomainCatalog;
import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CorporateLookupQueryServiceImpl Unit Tests")
class CorporateLookupQueryServiceImplTest {

    @Mock
    private SunatRucVerifierService sunatRucVerifierService;

    @Mock
    private CorporateDomainCatalog corporateDomainCatalog;

    @Mock
    private FinancialEntityRepository financialEntityRepository;

    @Mock
    private DealershipRepository dealershipRepository;

    private CorporateLookupQueryServiceImpl queryService;

    @BeforeEach
    void setUp() {
        queryService = new CorporateLookupQueryServiceImpl(
                sunatRucVerifierService,
                corporateDomainCatalog,
                financialEntityRepository,
                dealershipRepository
        );
    }

    @Test
    @DisplayName("Should successfully pre-fill profile data for a Bank")
    void shouldSuccessfullyPreFillProfileDataForBank() {
        String ruc = "20100047218";
        var rucInfo = new SunatRucInfo(ruc, "BANCO DE CREDITO DEL PERU", "ACTIVO", "HABIDO", "S.A.A.", "150101", "CAL. CENTENARIO 156", "6419");
        when(sunatRucVerifierService.verifyRuc(ruc)).thenReturn(Optional.of(rucInfo));
        when(corporateDomainCatalog.determineEntityType(rucInfo)).thenReturn(CorporateEntityType.FINANCIAL_INSTITUTION);
        when(corporateDomainCatalog.getAuthorizedDomains(ruc)).thenReturn(List.of("viabcp.com", "bcp.com.pe"));

        var entity = new FinancialEntity(new FinancialEntityId(UUID.randomUUID()), "user-1", ruc, "BANCO DE CREDITO DEL PERU", "https://logo.png", null, Collections.emptyList());
        when(financialEntityRepository.findByRuc(ruc)).thenReturn(Optional.of(entity));

        var resultOpt = queryService.lookupByRuc(ruc);

        assertTrue(resultOpt.isPresent());
        var res = resultOpt.get();
        assertEquals(ruc, res.ruc());
        assertEquals("FINANCIAL_INSTITUTION", res.entityType());
        assertEquals("ROLE_FINANCIAL_INSTITUTION", res.targetRole());
        assertEquals("BANCO DE CREDITO DEL PERU", res.suggestedName());
        assertEquals("CAL. CENTENARIO 156", res.fiscalAddress());
        assertEquals("https://logo.png", res.logoUrl());
        assertTrue(res.allowedEmailDomains().contains("viabcp.com"));
        assertTrue(res.eligibleForVerification());
    }

    @Test
    @DisplayName("Should successfully pre-fill profile data for a Dealership")
    void shouldSuccessfullyPreFillProfileDataForDealership() {
        String ruc = "20349887714";
        var rucInfo = new SunatRucInfo(ruc, "AUTOLAND S.A.", "ACTIVO", "HABIDO", "S.A.", "150101", "AV. JAVIER PRADO ESTE 5020", "4510");
        when(sunatRucVerifierService.verifyRuc(ruc)).thenReturn(Optional.of(rucInfo));
        when(corporateDomainCatalog.determineEntityType(rucInfo)).thenReturn(CorporateEntityType.DEALERSHIP);
        when(corporateDomainCatalog.getAuthorizedDomains(ruc)).thenReturn(List.of("autoland.com.pe"));

        var dealer = new Dealership("user-2", ruc, "AUTOLAND S.A.", "AV. JAVIER PRADO ESTE 5020", null, "info@autoland.com.pe", null, null, null, "https://dealer-logo.png", null);
        when(dealershipRepository.findByRuc(ruc)).thenReturn(Optional.of(dealer));

        var resultOpt = queryService.lookupByRuc(ruc);

        assertTrue(resultOpt.isPresent());
        var res = resultOpt.get();
        assertEquals(ruc, res.ruc());
        assertEquals("DEALERSHIP", res.entityType());
        assertEquals("ROLE_DEALER", res.targetRole());
        assertEquals("AUTOLAND S.A.", res.suggestedName());
        assertEquals("AV. JAVIER PRADO ESTE 5020", res.fiscalAddress());
        assertEquals("https://dealer-logo.png", res.logoUrl());
        assertTrue(res.allowedEmailDomains().contains("autoland.com.pe"));
        assertTrue(res.eligibleForVerification());
    }

    @Test
    @DisplayName("Should throw exception if RUC is not active or habido")
    void shouldThrowExceptionIfNotActiveOrHabido() {
        String ruc = "20100047218";
        var rucInfo = new SunatRucInfo(ruc, "EMPRESA BAJA", "BAJA", "NO HABIDO", "S.A.", "150101", "DIR", "6419");
        when(sunatRucVerifierService.verifyRuc(ruc)).thenReturn(Optional.of(rucInfo));

        assertThrows(DomainValidationException.class, () -> queryService.lookupByRuc(ruc));
    }

    @Test
    @DisplayName("Should throw exception if RUC is not a corporate financial or automotive entity")
    void shouldThrowExceptionIfNotCorporateEntity() {
        String ruc = "20100047218";
        var rucInfo = new SunatRucInfo(ruc, "RESTAURANTE PERUANO", "ACTIVO", "HABIDO", "S.A.", "150101", "DIR", "5610");
        when(sunatRucVerifierService.verifyRuc(ruc)).thenReturn(Optional.of(rucInfo));
        when(corporateDomainCatalog.determineEntityType(rucInfo)).thenReturn(CorporateEntityType.UNKNOWN);

        assertThrows(DomainValidationException.class, () -> queryService.lookupByRuc(ruc));
    }
}
