package com.smartfinance.smartfinancedriveplatform.crm.application.queryservices;

import com.smartfinance.smartfinancedriveplatform.crm.domain.model.aggregates.Prospect;
import com.smartfinance.smartfinancedriveplatform.crm.domain.model.aggregates.TestDrive;
import com.smartfinance.smartfinancedriveplatform.crm.domain.model.queries.*;

import java.util.List;
import java.util.Optional;

public interface CrmQueryService {

    List<Prospect> handle(GetProspectsForDealerQuery query);

    Optional<Prospect> handle(GetProspectByIdQuery query);

    Optional<TestDrive> handle(GetTestDriveByIdQuery query);

    List<TestDrive> handle(GetTestDrivesForUserQuery query);
}
