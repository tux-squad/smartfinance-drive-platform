package com.smartfinance.smartfinancedriveplatform.iam.domain.model.aggregates;

import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.Password;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.Roles;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.Username;
import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * User aggregate root in the IAM Bounded Context.
 * Controls access identity, encrypted credentials, and user roles.
 */
@Getter
public class User extends AbstractDomainAggregateRoot<User> {

    private Long id;
    private Username username;
    private Password password;
    private final List<Roles> roles = new ArrayList<>();

    /**
     * Reconstitution constructor for persistence adapters.
     */
    public User(Long id, Username username, Password password, List<Roles> roles) {
        this.id = id;
        this.username = username;
        this.password = password;
        if (roles != null) {
            this.roles.addAll(roles);
        }
    }

    /**
     * Creation constructor for new user registration.
     */
    public User(Username username, Password password) {
        this(username, password, List.of(Roles.ROLE_USER));
    }

    /**
     * Creation constructor specifying custom initial roles.
     */
    public User(Username username, Password password, List<Roles> roles) {
        setUsername(username);
        setPassword(password);
        if (roles != null && !roles.isEmpty()) {
            this.roles.addAll(roles);
        } else {
            this.roles.add(Roles.ROLE_USER);
        }
    }

    public void setUsername(Username username) {
        if (username == null) {
            throw new DomainValidationException("iam.error.username.required");
        }
        this.username = username;
    }

    public void setPassword(Password password) {
        if (password == null) {
            throw new DomainValidationException("iam.error.password.required");
        }
        this.password = password;
    }

    public List<Roles> getRoles() {
        return Collections.unmodifiableList(roles);
    }

    public User addRole(Roles role) {
        if (role == null) {
            throw new DomainValidationException("iam.error.role.required");
        }
        if (!this.roles.contains(role)) {
            this.roles.add(role);
        }
        return this;
    }

    public User removeRole(Roles role) {
        if (role != null) {
            this.roles.remove(role);
        }
        return this;
    }
}
