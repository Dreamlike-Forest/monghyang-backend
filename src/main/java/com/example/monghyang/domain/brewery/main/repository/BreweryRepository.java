package com.example.monghyang.domain.brewery.main.repository;

import com.example.monghyang.domain.brewery.main.entity.Brewery;
import com.example.monghyang.domain.users.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BreweryRepository extends JpaRepository<Brewery, Long> {
    @Query("select b.user.phone from Brewery b where b.id = :breweryId")
    Optional<String> findPhoneById(@Param("breweryId") Long breweryId);

    @Query("select b from Brewery b where b.user.id = :userId")
    Optional<Brewery> findByUserId(@Param("userId") Long userId);

}
