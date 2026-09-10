package com.smartfinance.smartfinancedriveplatform.scoring.application.internal.commandservices;

import com.smartfinance.smartfinancedriveplatform.scoring.application.commandservices.CreditScoreCommandService;
import com.smartfinance.smartfinancedriveplatform.scoring.domain.model.aggregates.CreditScore;
import com.smartfinance.smartfinancedriveplatform.scoring.domain.model.commands.DeleteCreditScoreCommand;
import com.smartfinance.smartfinancedriveplatform.scoring.domain.model.commands.EvaluateCreditScoreCommand;
import com.smartfinance.smartfinancedriveplatform.scoring.domain.repositories.CreditScoreRepository;
import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Implementation of CreditScoreCommandService application service.
 */
@Service
public class CreditScoreCommandServiceImpl implements CreditScoreCommandService {

    private final CreditScoreRepository creditScoreRepository;

    public CreditScoreCommandServiceImpl(CreditScoreRepository creditScoreRepository) {
        this.creditScoreRepository = creditScoreRepository;
    }

    @Override
    @Transactional
    public Optional<CreditScore> handle(EvaluateCreditScoreCommand command) {
        CreditScore creditScore = new CreditScore(
                command.profileId(),
                command.simulationId(),
                command.monthlyIncome(),
                command.projectedMonthlyInstallment()
        );

        CreditScore savedScore = creditScoreRepository.save(creditScore);
        return Optional.of(savedScore);
    }

    @Override
    @Transactional
    public void handle(DeleteCreditScoreCommand command) {
        if (!creditScoreRepository.existsById(command.scoreId())) {
            throw new DomainValidationException("scoring.error.creditScoreNotFound");
        }
        creditScoreRepository.deleteById(command.scoreId());
    }
}
