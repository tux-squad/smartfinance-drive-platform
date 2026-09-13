package com.smartfinance.smartfinancedriveplatform.iam.application.internal.queryservices;

import com.smartfinance.smartfinancedriveplatform.iam.domain.model.aggregates.User;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.queries.GetAllUsersQuery;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.queries.GetUserByIdQuery;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.queries.GetUserByUsernameQuery;
import com.smartfinance.smartfinancedriveplatform.iam.domain.repositories.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service implementation for handling IAM read queries.
 */
@Service
public class UserQueryServiceImpl implements UserQueryService {

    private final UserRepository userRepository;

    public UserQueryServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> handle(GetUserByIdQuery query) {
        return userRepository.findById(query.userId());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> handle(GetUserByUsernameQuery query) {
        return userRepository.findByUsername(query.username());
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> handle(GetAllUsersQuery query) {
        return userRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<User> handle(GetAllUsersQuery query, Pageable pageable) {
        return userRepository.findAll(pageable);
    }
}
