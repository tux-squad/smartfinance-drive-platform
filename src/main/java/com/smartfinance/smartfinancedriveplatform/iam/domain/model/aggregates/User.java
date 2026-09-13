package com.smartfinance.smartfinancedriveplatform.iam.domain.model.aggregates;

import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.Password;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.Roles;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.Username;
import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * User aggregate root in the IAM Bounded Context.
 * Controls access identity, encrypted credentials, user roles, and login lockout.
 */
@Getter
public class User extends AbstractDomainAggregateRoot<User> {

    private Long id;
    private Username username;
    private Password password;
    private final List<Roles> roles = new ArrayList<>();
    private int failedLoginAttempts;
    private boolean accountLocked;
    private LocalDateTime lockoutUntil;

    /**
     * Reconstitution constructor for persistence adapters.
     */
    public User(Long id, Username username, Password password, List<Roles> roles, int failedLoginAttempts, boolean accountLocked, LocalDateTime lockoutUntil) {
        this.id = id;
        this.username = username;
        this.password = password;
        if (roles != null) {
            this.roles.addAll(roles);
        }
        this.failedLoginAttempts = failedLoginAttempts;
        this.accountLocked = accountLocked;
        this.lockoutUntil = lockoutUntil;
    }

    public User(Long id, Username username, Password password, List<Roles> roles) {
        this(id, username, password, roles, 0, false, null);
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
        this.failedLoginAttempts = 0;
        this.accountLocked = false;
        this.lockoutUntil = null;
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

    public boolean isAccountLocked() {
        if (accountLocked && lockoutUntil != null) {
            if (LocalDateTime.now().isAfter(lockoutUntil)) {
                this.accountLocked = false;
                this.failedLoginAttempts = 0;
                this.lockoutUntil = null;
                return false;
            }
            return true;
        }
        return false;
    }

    public void recordFailedLoginAttempt() {
        this.failedLoginAttempts++;
        if (this.failedLoginAttempts >= 5) {
            this.accountLocked = true;
            this.lockoutUntil = LocalDateTime.now().plusMinutes(15);
        }
    }

    public void resetFailedLoginAttempts() {
        this.failedLoginAttempts = 0;
        this.accountLocked = false;
        this.lockoutUntil = null;
    }
}
