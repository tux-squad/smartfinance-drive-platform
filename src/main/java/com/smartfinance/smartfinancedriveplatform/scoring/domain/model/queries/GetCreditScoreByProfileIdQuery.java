package com.smartfinance.smartfinancedriveplatform.scoring.domain.model.queries;

/**
 * Query to retrieve credit score evaluations for a specific customer profile.
 */
public record GetCreditScoreByProfileIdQuery(String profileId) {}
