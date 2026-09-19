package com.smartfinance.smartfinancedriveplatform.consultations.application.internal.commandservices;

import com.smartfinance.smartfinancedriveplatform.consultations.application.commandservices.AiConsultationCommandService;
import com.smartfinance.smartfinancedriveplatform.consultations.domain.model.aggregates.AiConsultation;
import com.smartfinance.smartfinancedriveplatform.consultations.domain.model.commands.CreateAiConsultationCommand;
import com.smartfinance.smartfinancedriveplatform.consultations.domain.repositories.AiConsultationRepository;
import com.smartfinance.smartfinancedriveplatform.consultations.domain.services.AiAdvisorEngine;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AiConsultationCommandServiceImpl implements AiConsultationCommandService {

    private final AiConsultationRepository repository;
    private final AiAdvisorEngine aiAdvisorEngine;

    public AiConsultationCommandServiceImpl(AiConsultationRepository repository, AiAdvisorEngine aiAdvisorEngine) {
        this.repository = repository;
        this.aiAdvisorEngine = aiAdvisorEngine;
    }

    @Override
    @Transactional
    public Optional<AiConsultation> handle(CreateAiConsultationCommand command) {
        var advice = aiAdvisorEngine.generateAdvice(command.prompt(), command.monthlyIncome(), command.maxBudget());
        AiConsultation consultation = new AiConsultation(
                command.userId(),
                command.prompt(),
                command.monthlyIncome(),
                command.maxBudget(),
                advice.recommendationText(),
                advice.recommendedVehicleCategory(),
                advice.estimatedMaxMonthlyFee()
        );
        AiConsultation saved = repository.save(consultation);
        return Optional.of(saved);
    }
}
