package com.smartfinance.smartfinancedriveplatform.crm.application.internal.queryservices;

import com.smartfinance.smartfinancedriveplatform.crm.application.queryservices.CrmQueryService;
import com.smartfinance.smartfinancedriveplatform.crm.domain.model.aggregates.Prospect;
import com.smartfinance.smartfinancedriveplatform.crm.domain.model.aggregates.TestDrive;
import com.smartfinance.smartfinancedriveplatform.crm.domain.model.queries.*;
import com.smartfinance.smartfinancedriveplatform.crm.domain.repositories.ProspectRepository;
import com.smartfinance.smartfinancedriveplatform.crm.domain.repositories.TestDriveRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CrmQueryServiceImpl implements CrmQueryService {

    private final ProspectRepository prospectRepository;
    private final TestDriveRepository testDriveRepository;

    public CrmQueryServiceImpl(ProspectRepository prospectRepository, TestDriveRepository testDriveRepository) {
        this.prospectRepository = prospectRepository;
        this.testDriveRepository = testDriveRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Prospect> handle(GetProspectsForDealerQuery query) {
        return prospectRepository.findAllByDealerUserId(query.dealerUserId());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Prospect> handle(GetProspectByIdQuery query) {
        return prospectRepository.findById(query.prospectId());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TestDrive> handle(GetTestDriveByIdQuery query) {
        return testDriveRepository.findById(query.testDriveId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TestDrive> handle(GetTestDrivesForUserQuery query) {
        return testDriveRepository.findAllByBuyerUserId(query.userId());
    }
}
