package com.smartfinance.smartfinancedriveplatform.financing.domain.repositories;

import com.smartfinance.smartfinancedriveplatform.financing.domain.model.aggregates.CreditApplication;
import com.smartfinance.smartfinancedriveplatform.financing.domain.model.valueobjects.CreditApplicationId;

import java.util.List;
import java.util.Optional;

/**
 * Domain repository interface for CreditApplication operations.
 */
public interface CreditApplicationRepository {

    CreditApplication save(CreditApplication creditApplication);

    Optional<CreditApplication> findById(CreditApplicationId id);

    List<CreditApplication> findAllByApplicantUserId(String applicantUserId);

    boolean existsById(CreditApplicationId id);
}
