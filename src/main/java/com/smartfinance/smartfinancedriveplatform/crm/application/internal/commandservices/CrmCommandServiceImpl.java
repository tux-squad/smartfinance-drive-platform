package com.smartfinance.smartfinancedriveplatform.crm.application.internal.commandservices;

import com.smartfinance.smartfinancedriveplatform.crm.application.commandservices.CrmCommandService;
import com.smartfinance.smartfinancedriveplatform.crm.domain.model.aggregates.Prospect;
import com.smartfinance.smartfinancedriveplatform.crm.domain.model.aggregates.TestDrive;
import com.smartfinance.smartfinancedriveplatform.crm.domain.model.commands.*;
import com.smartfinance.smartfinancedriveplatform.crm.domain.model.entities.ProspectNote;
import com.smartfinance.smartfinancedriveplatform.crm.domain.repositories.ProspectRepository;
import com.smartfinance.smartfinancedriveplatform.crm.domain.repositories.TestDriveRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class CrmCommandServiceImpl implements CrmCommandService {

    private final ProspectRepository prospectRepository;
    private final TestDriveRepository testDriveRepository;

    public CrmCommandServiceImpl(ProspectRepository prospectRepository, TestDriveRepository testDriveRepository) {
        this.prospectRepository = prospectRepository;
        this.testDriveRepository = testDriveRepository;
    }

    @Override
    @Transactional
    public Optional<Prospect> handle(CreateProspectCommand command) {
        Prospect prospect = new Prospect(
                command.dealerUserId(),
                command.buyerUserId(),
                command.fullName(),
                command.email(),
                command.phone(),
                command.interestedVehicleId(),
                command.salesAgentId()
        );
        Prospect saved = prospectRepository.save(prospect);
        return Optional.of(saved);
    }

    @Override
    @Transactional
    public Optional<ProspectNote> handle(AddProspectNoteCommand command) {
        var prospectOpt = prospectRepository.findById(command.prospectId());
        if (prospectOpt.isEmpty()) {
            return Optional.empty();
        }
        var prospect = prospectOpt.get();
        prospect.addNote(command.authorUserId(), command.noteText());
        prospectRepository.save(prospect);
        var lastNote = prospect.getNotes().get(prospect.getNotes().size() - 1);
        return Optional.of(lastNote);
    }

    @Override
    @Transactional
    public Optional<Prospect> handle(UpdateProspectStatusCommand command) {
        var prospectOpt = prospectRepository.findById(command.prospectId());
        if (prospectOpt.isEmpty()) {
            return Optional.empty();
        }
        var prospect = prospectOpt.get();
        prospect.setStatus(command.status());
        Prospect saved = prospectRepository.save(prospect);
        return Optional.of(saved);
    }

    @Override
    @Transactional
    public Optional<TestDrive> handle(ScheduleTestDriveCommand command) {
        TestDrive testDrive = new TestDrive(
                command.buyerUserId(),
                command.vehicleId(),
                command.dealershipId(),
                command.scheduledDateTime(),
                command.notes()
        );
        TestDrive saved = testDriveRepository.save(testDrive);
        return Optional.of(saved);
    }
}
