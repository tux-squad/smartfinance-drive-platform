package com.smartfinance.smartfinancedriveplatform.iam.domain.model.entities;

import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.Roles;
import lombok.Getter;

/**
 * Domain entity representing a user security role.
 */
@Getter
public class Role {

    private final Long id;
    private final Roles name;

    public Role(Long id, Roles name) {
        this.id = id;
        this.name = name;
    }

    public Role(Roles name) {
        this.id = null;
        this.name = name;
    }

    public String getStringName() {
        return name.name();
    }
}
