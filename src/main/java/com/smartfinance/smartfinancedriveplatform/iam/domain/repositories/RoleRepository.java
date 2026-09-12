package com.smartfinance.smartfinancedriveplatform.iam.domain.repositories;

import com.smartfinance.smartfinancedriveplatform.iam.domain.model.entities.Role;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.Roles;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing Role domain entities.
 */
public interface RoleRepository {

    Optional<Role> findByName(Roles name);

    Role save(Role role);

    boolean existsByName(Roles name);

    List<Role> findAll();
}
