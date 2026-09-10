package com.smartfinance.smartfinancedriveplatform.iam.domain.repositories;

import com.smartfinance.smartfinancedriveplatform.iam.domain.model.aggregates.User;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.Username;

import java.util.List;
import java.util.Optional;

/**
 * Domain repository interface for User aggregate management.
 */
public interface UserRepository {

    User save(User user);

    Optional<User> findByUsername(Username username);

    boolean existsByUsername(Username username);

    Optional<User> findById(Long id);

    List<User> findAll();
}
