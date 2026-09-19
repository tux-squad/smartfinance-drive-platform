package com.smartfinance.smartfinancedriveplatform.crm.domain.repositories;

import com.smartfinance.smartfinancedriveplatform.crm.domain.model.aggregates.TestDrive;
import com.smartfinance.smartfinancedriveplatform.crm.domain.model.valueobjects.TestDriveId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TestDriveRepository {
    TestDrive save(TestDrive testDrive);
    Optional<TestDrive> findById(TestDriveId id);
    List<TestDrive> findAllByBuyerUserId(String buyerUserId);
    List<TestDrive> findAllByDealershipId(UUID dealershipId);
}
