package com.smartfinance.smartfinancedriveplatform.consultations.application.internal.queryservices;

import com.smartfinance.smartfinancedriveplatform.consultations.application.queryservices.AiConsultationQueryService;
import com.smartfinance.smartfinancedriveplatform.consultations.domain.model.aggregates.AiConsultation;
import com.smartfinance.smartfinancedriveplatform.consultations.domain.model.queries.GetAiConsultationHistoryForUserQuery;
import com.smartfinance.smartfinancedriveplatform.consultations.domain.repositories.AiConsultationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AiConsultationQueryServiceImpl implements AiConsultationQueryService {

    private final AiConsultationRepository repository;

    public AiConsultationQueryServiceImpl(AiConsultationRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AiConsultation> handle(GetAiConsultationHistoryForUserQuery query) {
        return repository.findAllByUserIdOrderByCreatedAtDesc(query.userId());
    }
}
