package com.smartfinance.smartfinancedriveplatform.partners.application.queryservices;

import com.smartfinance.smartfinancedriveplatform.partners.domain.model.aggregates.FinancialEntity;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.queries.GetAllFinancialEntitiesQuery;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.queries.GetFinancialEntityByIdQuery;

import java.util.List;
import java.util.Optional;

/**
 * Interface declaring query operations for the FinancialEntity application layer.
 */
public interface FinancialEntityQueryService {

    /**
     * Handles retrieving a financial entity by its unique ID.
     *
     * @param query The query containing the ID.
     * @return An Optional containing the financial entity if found.
     */
    Optional<FinancialEntity> handle(GetFinancialEntityByIdQuery query);

    /**
     * Handles retrieving all financial entities.
     *
     * @param query The query.
     * @return A list of all financial entities.
     */
    List<FinancialEntity> handle(GetAllFinancialEntitiesQuery query);
}
