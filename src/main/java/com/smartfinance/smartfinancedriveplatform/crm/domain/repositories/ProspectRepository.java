package com.smartfinance.smartfinancedriveplatform.crm.domain.repositories;

import com.smartfinance.smartfinancedriveplatform.crm.domain.model.aggregates.Prospect;
import com.smartfinance.smartfinancedriveplatform.crm.domain.model.valueobjects.ProspectId;

import java.util.List;
import java.util.Optional;

public interface ProspectRepository {
    Prospect save(Prospect prospect);
    Optional<Prospect> findById(ProspectId id);
    List<Prospect> findAllByDealerUserId(String dealerUserId);
    boolean existsById(ProspectId id);
}
