package com.smartfinance.smartfinancedriveplatform.iam.application.internal.queryservices;

import com.smartfinance.smartfinancedriveplatform.iam.domain.model.aggregates.User;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.queries.GetAllUsersQuery;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.queries.GetUserByIdQuery;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.queries.GetUserByUsernameQuery;

import java.util.List;
import java.util.Optional;

/**
 * Application query service interface for user read queries.
 */
public interface UserQueryService {

    Optional<User> handle(GetUserByIdQuery query);

    Optional<User> handle(GetUserByUsernameQuery query);

    List<User> handle(GetAllUsersQuery query);
}
