package com.example.monghyang.domain.brewery.repository;

import com.example.monghyang.domain.brewery.entity.BreweryClosedDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface BreweryClosedDateRepository extends JpaRepository<BreweryClosedDate,Long> {
    @Modifying
    @Query("delete from BreweryClosedDate bcd where bcd.brewery.id = :breweryId and bcd.closedDate = :closedDate")
    int deleteByBreweryIdAndClosedDate(@Param("breweryId") Long breweryId, @Param("closedDate") LocalDate closedDate);

    Optional<BreweryClosedDate> findByBreweryIdAndClosedDate(Long breweryId, LocalDate closedDate);
}
