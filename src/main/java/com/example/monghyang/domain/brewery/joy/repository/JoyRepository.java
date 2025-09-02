package com.example.monghyang.domain.brewery.joy.repository;

import com.example.monghyang.domain.brewery.joy.entity.Joy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface JoyRepository extends JpaRepository<Joy, Long> {

    @Query("select j from Joy j where j.brewery.id = :breweryId and j.isDeleted = false")
    List<Joy> findActiveByBreweryId(@Param("breweryId") Long breweryId);

    @Query("select j from Joy j where j.id = :joyId and j.brewery.id = :breweryId")
    Optional<Joy> findByBreweryIdAndJoyId(@Param("breweryId") Long breweryId, @Param("joyId") Long joyId);
}
