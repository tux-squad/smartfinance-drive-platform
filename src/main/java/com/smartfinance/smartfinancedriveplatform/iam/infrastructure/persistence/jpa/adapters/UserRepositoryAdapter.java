package com.smartfinance.smartfinancedriveplatform.iam.infrastructure.persistence.jpa.adapters;

import com.smartfinance.smartfinancedriveplatform.iam.domain.model.aggregates.User;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.Password;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.Username;
import com.smartfinance.smartfinancedriveplatform.iam.domain.repositories.UserRepository;
import com.smartfinance.smartfinancedriveplatform.iam.infrastructure.persistence.jpa.entities.UserJPAEntity;
import com.smartfinance.smartfinancedriveplatform.iam.infrastructure.persistence.jpa.repositories.SpringDataUserRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Adapter implementing {@link UserRepository} by delegating to Spring Data JPA.
 */
@Component
public class UserRepositoryAdapter implements UserRepository {

    private final SpringDataUserRepository repository;

    public UserRepositoryAdapter(SpringDataUserRepository repository) {
        this.repository = repository;
    }

    @Override
    public User save(User user) {
        UserJPAEntity entity = toEntity(user);
        UserJPAEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<User> findByUsername(Username username) {
        return repository.findByUsername(username.username())
                .map(this::toDomain);
    }

    @Override
    public boolean existsByUsername(Username username) {
        return repository.existsByUsername(username.username());
    }

    @Override
    public Optional<User> findById(Long id) {
        return repository.findById(id)
                .map(this::toDomain);
    }

    @Override
    public List<User> findAll() {
        return repository.findAll().stream()
                .map(this::toDomain)
                .toList();
    }

    private UserJPAEntity toEntity(User domain) {
        UserJPAEntity entity = new UserJPAEntity(
                domain.getUsername().username(),
                domain.getPassword().password(),
                domain.getRoles()
        );
        if (domain.getId() != null) {
            entity.setId(domain.getId());
        }
        return entity;
    }

    private User toDomain(UserJPAEntity entity) {
        return new User(
                entity.getId(),
                new Username(entity.getUsername()),
                new Password(entity.getPassword()),
                entity.getRoles()
        );
    }
}
