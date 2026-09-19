package com.smartfinance.smartfinancedriveplatform.crm.application.commandservices;

import com.smartfinance.smartfinancedriveplatform.crm.domain.model.aggregates.Prospect;
import com.smartfinance.smartfinancedriveplatform.crm.domain.model.aggregates.TestDrive;
import com.smartfinance.smartfinancedriveplatform.crm.domain.model.commands.*;
import com.smartfinance.smartfinancedriveplatform.crm.domain.model.entities.ProspectNote;

import java.util.Optional;

public interface CrmCommandService {

    Optional<Prospect> handle(CreateProspectCommand command);

    Optional<ProspectNote> handle(AddProspectNoteCommand command);

    Optional<Prospect> handle(UpdateProspectStatusCommand command);

    Optional<TestDrive> handle(ScheduleTestDriveCommand command);

    Optional<TestDrive> handle(UpdateTestDriveStatusCommand command);

    void handle(CancelTestDriveCommand command);
}
