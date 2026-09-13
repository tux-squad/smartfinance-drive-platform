package com.smartfinance.smartfinancedriveplatform.iam.infrastructure.persistence.jpa.entities;

import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.Roles;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity representing the 'users' table in the database.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class UserJPAEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false, unique = true, length = 100)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private List<Roles> roles = new ArrayList<>();

    @Column(name = "failed_login_attempts", nullable = false)
    private int failedLoginAttempts = 0;

    @Column(name = "account_locked", nullable = false)
    private boolean accountLocked = false;

    @Column(name = "lockout_until")
    private LocalDateTime lockoutUntil;

    public UserJPAEntity(String username, String password, List<Roles> roles) {
        this.username = username;
        this.password = password;
        if (roles != null) {
            this.roles.addAll(roles);
        }
    }
}
