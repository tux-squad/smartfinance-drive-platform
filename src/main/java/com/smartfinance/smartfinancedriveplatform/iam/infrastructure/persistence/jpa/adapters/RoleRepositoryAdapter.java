package com.smartfinance.smartfinancedriveplatform.iam.infrastructure.persistence.jpa.adapters;

import com.smartfinance.smartfinancedriveplatform.iam.domain.model.entities.Role;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.Roles;
import com.smartfinance.smartfinancedriveplatform.iam.domain.repositories.RoleRepository;
import com.smartfinance.smartfinancedriveplatform.iam.infrastructure.persistence.jpa.entities.RolePersistenceEntity;
import com.smartfinance.smartfinancedriveplatform.iam.infrastructure.persistence.jpa.repositories.SpringDataRoleRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adapter implementation for RoleRepository using Spring Data JPA.
 */
@Component
public class RoleRepositoryAdapter implements RoleRepository {

    private final SpringDataRoleRepository springDataRoleRepository;

    public RoleRepositoryAdapter(SpringDataRoleRepository springDataRoleRepository) {
        this.springDataRoleRepository = springDataRoleRepository;
    }

    @Override
    public Optional<Role> findByName(Roles name) {
        return springDataRoleRepository.findByName(name)
                .map(entity -> new Role(entity.getId(), entity.getName()));
    }

    @Override
    public Role save(Role role) {
        RolePersistenceEntity entity = new RolePersistenceEntity(role.getName());
        if (role.getId() != null) {
            entity.setId(role.getId());
        }
        RolePersistenceEntity saved = springDataRoleRepository.save(entity);
        return new Role(saved.getId(), saved.getName());
    }

    @Override
    public boolean existsByName(Roles name) {
        return springDataRoleRepository.existsByName(name);
    }

    @Override
    public List<Role> findAll() {
        return springDataRoleRepository.findAll().stream()
                .map(entity -> new Role(entity.getId(), entity.getName()))
                .collect(Collectors.toList());
    }
}
