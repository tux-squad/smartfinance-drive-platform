package com.smartfinance.smartfinancedriveplatform.iam.domain.model.queries;

import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.Username;

/**
 * Query to retrieve a User by Username.
 */
public record GetUserByUsernameQuery(Username username) {
}
