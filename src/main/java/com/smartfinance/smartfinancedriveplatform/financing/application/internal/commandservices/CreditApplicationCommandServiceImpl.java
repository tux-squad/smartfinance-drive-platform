package com.smartfinance.smartfinancedriveplatform.financing.application.internal.commandservices;

import com.smartfinance.smartfinancedriveplatform.financing.application.commandservices.CreditApplicationCommandService;
import com.smartfinance.smartfinancedriveplatform.financing.domain.model.aggregates.CreditApplication;
import com.smartfinance.smartfinancedriveplatform.financing.domain.model.commands.CreateCreditApplicationCommand;
import com.smartfinance.smartfinancedriveplatform.financing.domain.model.commands.UpdateCreditApplicationStatusCommand;
import com.smartfinance.smartfinancedriveplatform.financing.domain.repositories.CreditApplicationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Implementation of CreditApplicationCommandService.
 */
@Service
public class CreditApplicationCommandServiceImpl implements CreditApplicationCommandService {

    private final CreditApplicationRepository creditApplicationRepository;

    public CreditApplicationCommandServiceImpl(CreditApplicationRepository creditApplicationRepository) {
        this.creditApplicationRepository = creditApplicationRepository;
    }

    @Override
    @Transactional
    public Optional<CreditApplication> handle(CreateCreditApplicationCommand command) {
        CreditApplication application = new CreditApplication(
                command.applicantUserId(),
                command.vehicleId(),
                command.financialEntityId(),
                command.simulationId(),
                command.requestedAmount(),
                command.downPayment(),
                command.termMonths(),
                command.monthlyIncome(),
                command.employmentStatus()
        );
        CreditApplication saved = creditApplicationRepository.save(application);
        return Optional.of(saved);
    }

    @Override
    @Transactional
    public Optional<CreditApplication> handle(UpdateCreditApplicationStatusCommand command) {
        var applicationOpt = creditApplicationRepository.findById(command.creditApplicationId());
        if (applicationOpt.isEmpty()) {
            return Optional.empty();
        }
        var application = applicationOpt.get();
        application.updateStatus(command.status(), command.notes());
        CreditApplication saved = creditApplicationRepository.save(application);
        return Optional.of(saved);
    }
}
