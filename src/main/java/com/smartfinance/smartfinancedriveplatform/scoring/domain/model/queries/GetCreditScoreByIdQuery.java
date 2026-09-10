package com.smartfinance.smartfinancedriveplatform.scoring.domain.model.queries;

import com.smartfinance.smartfinancedriveplatform.scoring.domain.model.valueobjects.ScoreId;

/**
 * Query to retrieve a credit score evaluation by ScoreId.
 */
public record GetCreditScoreByIdQuery(ScoreId scoreId) {}
