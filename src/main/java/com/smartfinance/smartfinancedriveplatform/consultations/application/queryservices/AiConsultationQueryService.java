package com.smartfinance.smartfinancedriveplatform.consultations.application.queryservices;

import com.smartfinance.smartfinancedriveplatform.consultations.domain.model.aggregates.AiConsultation;
import com.smartfinance.smartfinancedriveplatform.consultations.domain.model.queries.GetAiConsultationHistoryForUserQuery;

import java.util.List;

public interface AiConsultationQueryService {
    List<AiConsultation> handle(GetAiConsultationHistoryForUserQuery query);
}
