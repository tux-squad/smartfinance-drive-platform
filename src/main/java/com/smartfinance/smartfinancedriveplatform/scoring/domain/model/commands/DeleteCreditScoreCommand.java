package com.smartfinance.smartfinancedriveplatform.scoring.domain.model.commands;

import com.smartfinance.smartfinancedriveplatform.scoring.domain.model.valueobjects.ScoreId;

/**
 * Command to delete a credit score evaluation.
 */
public record DeleteCreditScoreCommand(ScoreId scoreId) {}
