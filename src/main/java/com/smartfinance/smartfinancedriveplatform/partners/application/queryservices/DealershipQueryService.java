package com.smartfinance.smartfinancedriveplatform.partners.application.queryservices;

import com.smartfinance.smartfinancedriveplatform.partners.domain.model.aggregates.Dealership;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.queries.GetAllDealershipsQuery;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.queries.GetDealershipByIdQuery;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.queries.GetDealershipByUserIdQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * Interface declaring query operations for Dealership application layer.
 */
public interface DealershipQueryService {

    Optional<Dealership> handle(GetDealershipByIdQuery query);

    Optional<Dealership> handle(GetDealershipByUserIdQuery query);

    Page<Dealership> handle(GetAllDealershipsQuery query, Pageable pageable);
}
