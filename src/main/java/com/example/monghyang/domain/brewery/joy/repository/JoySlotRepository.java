package com.example.monghyang.domain.brewery.joy.repository;

import com.example.monghyang.domain.brewery.joy.entity.JoySlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface JoySlotRepository extends JpaRepository<JoySlot, Integer> {
    @Query("select js from JoySlot js where js.joy.id = :joyId and js.reservation = :reservation")
    Optional<JoySlot> findByJoyIdAndReservation(@Param("joyId") Long joyId, @Param("reservation") LocalDateTime reservation);
}
