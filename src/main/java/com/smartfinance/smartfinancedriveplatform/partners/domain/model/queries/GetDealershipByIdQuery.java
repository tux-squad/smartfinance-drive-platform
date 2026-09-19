package com.smartfinance.smartfinancedriveplatform.partners.domain.model.queries;

import com.smartfinance.smartfinancedriveplatform.partners.domain.model.valueobjects.DealershipId;

/**
 * Query to retrieve a Dealership by its unique identifier.
 */
public record GetDealershipByIdQuery(DealershipId dealershipId) {}
