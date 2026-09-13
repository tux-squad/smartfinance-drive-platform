package com.smartfinance.smartfinancedriveplatform.iam.domain.repositories;

import com.smartfinance.smartfinancedriveplatform.iam.domain.model.aggregates.User;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.Username;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing User aggregates in the IAM Bounded Context.
 */
public interface UserRepository {

    User save(User user);

    Optional<User> findByUsername(Username username);

    boolean existsByUsername(Username username);

    Optional<User> findById(Long id);

    List<User> findAll();

    Page<User> findAll(Pageable pageable);
}
