package com.smartfinance.smartfinancedriveplatform.financing.application.queryservices;

import com.smartfinance.smartfinancedriveplatform.financing.domain.model.aggregates.CreditApplication;
import com.smartfinance.smartfinancedriveplatform.financing.domain.model.queries.GetCreditApplicationByIdQuery;
import com.smartfinance.smartfinancedriveplatform.financing.domain.model.queries.GetCreditApplicationsByApplicantQuery;

import java.util.List;
import java.util.Optional;

/**
 * Interface declaring query operations for CreditApplication.
 */
public interface CreditApplicationQueryService {

    Optional<CreditApplication> handle(GetCreditApplicationByIdQuery query);

    List<CreditApplication> handle(GetCreditApplicationsByApplicantQuery query);
}
