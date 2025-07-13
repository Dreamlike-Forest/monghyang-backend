package com.example.monghyang.domain.users.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.monghyang.domain.users.entity.Users;

import java.util.Optional;

public interface UsersRepository extends JpaRepository<Users, Long> {
    Optional<Users> findByEmail(String email);
    Optional<Users> findByoAuth2Id(String oAuth2Id);
}
