package com.smartfinance.smartfinancedriveplatform.iam.domain.repositories;

import com.smartfinance.smartfinancedriveplatform.iam.domain.model.aggregates.SalesAgent;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.SalesAgentId;

import java.util.List;
import java.util.Optional;

public interface SalesAgentRepository {
    SalesAgent save(SalesAgent salesAgent);
    Optional<SalesAgent> findById(SalesAgentId id);
    List<SalesAgent> findByDealerUserId(String dealerUserId);
}
