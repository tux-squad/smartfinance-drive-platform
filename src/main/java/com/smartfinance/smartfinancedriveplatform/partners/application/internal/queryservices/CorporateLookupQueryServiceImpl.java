package com.smartfinance.smartfinancedriveplatform.partners.application.internal.queryservices;

import com.smartfinance.smartfinancedriveplatform.partners.application.outboundservices.SunatRucVerifierService;
import com.smartfinance.smartfinancedriveplatform.partners.application.queryservices.CorporateLookupQueryService;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.valueobjects.CorporateEntityType;
import com.smartfinance.smartfinancedriveplatform.partners.domain.repositories.DealershipRepository;
import com.smartfinance.smartfinancedriveplatform.partners.domain.repositories.FinancialEntityRepository;
import com.smartfinance.smartfinancedriveplatform.partners.domain.services.CorporateDomainCatalog;
import com.smartfinance.smartfinancedriveplatform.partners.interfaces.rest.resources.CorporateLookupResource;
import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Implementation of CorporateLookupQueryService.
 * Validates RUC with SUNAT, classifies institution type, and pre-fills corporate profile fields.
 */
@Service
public class CorporateLookupQueryServiceImpl implements CorporateLookupQueryService {

    private final SunatRucVerifierService sunatRucVerifierService;
    private final CorporateDomainCatalog corporateDomainCatalog;
    private final FinancialEntityRepository financialEntityRepository;
    private final DealershipRepository dealershipRepository;

    public CorporateLookupQueryServiceImpl(SunatRucVerifierService sunatRucVerifierService,
                                          CorporateDomainCatalog corporateDomainCatalog,
                                          FinancialEntityRepository financialEntityRepository,
                                          DealershipRepository dealershipRepository) {
        this.sunatRucVerifierService = sunatRucVerifierService;
        this.corporateDomainCatalog = corporateDomainCatalog;
        this.financialEntityRepository = financialEntityRepository;
        this.dealershipRepository = dealershipRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CorporateLookupResource> lookupByRuc(String ruc) {
        if (ruc == null || !ruc.matches("^\\d{11}$")) {
            throw new DomainValidationException("partners.error.dealership.ruc.invalid");
        }

        var rucInfoOpt = sunatRucVerifierService.verifyRuc(ruc);
        if (rucInfoOpt.isEmpty()) {
            return Optional.empty();
        }

        var rucInfo = rucInfoOpt.get();
        if (!rucInfo.isActiveAndHabido()) {
            throw new DomainValidationException("iam.error.sunat.rucNotActiveOrHabido");
        }

        CorporateEntityType entityType = corporateDomainCatalog.determineEntityType(rucInfo);
        if (entityType == CorporateEntityType.UNKNOWN) {
            throw new DomainValidationException("iam.error.sunat.notAuthorizedCorporateEntity");
        }

        Set<String> domainsSet = new LinkedHashSet<>(corporateDomainCatalog.getAuthorizedDomains(ruc));
        String logoUrl = null;

        if (entityType == CorporateEntityType.FINANCIAL_INSTITUTION) {
            var entityOpt = financialEntityRepository.findByRuc(ruc);
            if (entityOpt.isEmpty()) {
                entityOpt = financialEntityRepository.findByName(rucInfo.razonSocial());
            }
            if (entityOpt.isPresent()) {
                var entity = entityOpt.get();
                domainsSet.addAll(entity.getAllowedDomains());
                logoUrl = entity.getLogoUrl();
            }
        } else if (entityType == CorporateEntityType.DEALERSHIP) {
            var dealerOpt = dealershipRepository.findByRuc(ruc);
            if (dealerOpt.isEmpty()) {
                dealerOpt = dealershipRepository.findByName(rucInfo.razonSocial());
            }
            if (dealerOpt.isPresent()) {
                var dealer = dealerOpt.get();
                domainsSet.addAll(dealer.getAllowedDomains());
                logoUrl = dealer.getLogoUrl();
            }
        }

        return Optional.of(new CorporateLookupResource(
                rucInfo.ruc(),
                entityType.name(),
                entityType.getTargetRole(),
                rucInfo.razonSocial(),
                rucInfo.direccion(),
                rucInfo.ubigeo(),
                new ArrayList<>(domainsSet),
                logoUrl,
                true
        ));
    }
}
