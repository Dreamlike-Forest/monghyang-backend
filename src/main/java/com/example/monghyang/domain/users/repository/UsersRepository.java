package com.example.monghyang.domain.users.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.monghyang.domain.users.entity.Users;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UsersRepository extends JpaRepository<Users, Long> {
    @Query("select u from Users u join fetch u.role where u.email = :email")
    Optional<Users> findByEmail(@Param("email") String email);
    Optional<Users> findByoAuth2Id(String oAuth2Id);
}
