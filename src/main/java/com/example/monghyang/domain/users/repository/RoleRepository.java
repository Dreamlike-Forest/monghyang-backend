package com.example.monghyang.domain.users.repository;

import com.example.monghyang.domain.users.entity.Role;
import com.example.monghyang.domain.users.entity.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByName(RoleType name);
}
