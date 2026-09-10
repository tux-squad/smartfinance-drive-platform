package com.smartfinance.smartfinancedriveplatform.partners.interfaces.rest.transform;

import com.smartfinance.smartfinancedriveplatform.partners.domain.model.aggregates.FinancialEntity;
import com.smartfinance.smartfinancedriveplatform.partners.interfaces.rest.resources.FinancialEntityResource;
import com.smartfinance.smartfinancedriveplatform.partners.interfaces.rest.resources.RateBenchmarkResource;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Assembler to convert a FinancialEntity domain aggregate root into a FinancialEntityResource response DTO.
 */
public final class FinancialEntityResourceFromEntityAssembler {

    private FinancialEntityResourceFromEntityAssembler() {}

    /**
     * Converts a FinancialEntity domain aggregate to a FinancialEntityResource response DTO.
     *
     * @param entity The financial entity domain aggregate.
     * @return The resource DTO.
     */
    public static FinancialEntityResource toResourceFromEntity(FinancialEntity entity) {
        List<RateBenchmarkResource> benchmarkResources = entity.getRateBenchmarks().stream()
            .map(b -> new RateBenchmarkResource(
                b.getId().value(),
                b.getRateType(),
                b.getAnnualRate().value(),
                b.getCurrency(),
                b.getSourceLabel(),
                b.getSourceUrl(),
                b.getEffectiveFrom()
            ))
            .collect(Collectors.toList());

        return new FinancialEntityResource(
            entity.getId().value(),
            entity.getName(),
            benchmarkResources
        );
    }
}
