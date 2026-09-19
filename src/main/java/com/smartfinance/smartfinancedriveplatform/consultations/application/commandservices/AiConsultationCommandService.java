package com.smartfinance.smartfinancedriveplatform.consultations.application.commandservices;

import com.smartfinance.smartfinancedriveplatform.consultations.domain.model.aggregates.AiConsultation;
import com.smartfinance.smartfinancedriveplatform.consultations.domain.model.commands.CreateAiConsultationCommand;

import java.util.Optional;

public interface AiConsultationCommandService {
    Optional<AiConsultation> handle(CreateAiConsultationCommand command);
}
