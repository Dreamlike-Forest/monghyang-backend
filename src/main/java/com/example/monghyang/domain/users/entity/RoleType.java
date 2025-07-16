package com.example.monghyang.domain.users.entity;

public enum RoleType {
    ROLE_ADMIN, ROLE_BREWERY, ROLE_SELLER, ROLE_USER;
    public String getRoleName() {
        return name();
    }
}
