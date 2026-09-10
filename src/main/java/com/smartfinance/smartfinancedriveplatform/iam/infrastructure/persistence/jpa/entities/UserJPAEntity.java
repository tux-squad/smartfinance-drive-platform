package com.smartfinance.smartfinancedriveplatform.iam.infrastructure.persistence.jpa.entities;

import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.Roles;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private List<Roles> roles = new ArrayList<>();

    public UserJPAEntity(String username, String password, List<Roles> roles) {
        this.username = username;
        this.password = password;
        if (roles != null) {
            this.roles.addAll(roles);
        }
    }
}
