package com.example.monghyang.domain.joy.repository;

import com.example.monghyang.domain.joy.dto.ResJoyOrderDto;
import com.example.monghyang.domain.joy.entity.JoyOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface JoyOrderRepository extends JpaRepository<JoyOrder, Long> {

    @Query("select jo from JoyOrder jo join fetch jo.users where jo.pgOrderId = :pgOrderId")
    Optional<JoyOrder> findByPgOrderId(@Param("pgOrderId") UUID pgOrderId);

    @Query("select jo from JoyOrder jo join jo.joy.brewery b where jo.id = :joyOrderId and b.user.id = :userId")
    Optional<JoyOrder> findByIdAndBreweryUserId(@Param("joyOrderId") Long joyOrderId, @Param("userId") Long userId);

    @Query("select jo from JoyOrder jo where jo.id = :joyOrderId and jo.users.id = :userId")
    Optional<JoyOrder> findByIdAndUserId(@Param("joyOrderId") Long joyOrderId, @Param("userId") Long userId);

    @Query("select new com.example.monghyang.domain.joy.dto.ResJoyOrderDto(jo.id, jo.users.id, j.id, j.name, jo.count, jo.totalAmount, jo.payerName, jo.payerPhone, jo.createdAt, jo.reservation, jo.joyPaymentStatus) from JoyOrder jo join jo.joy j on j.brewery.id = :breweryId")
    Page<ResJoyOrderDto> findByBreweryId(@Param("breweryId") Long breweryId, Pageable pageable);

    @Query("select new com.example.monghyang.domain.joy.dto.ResJoyOrderDto(jo.id, jo.users.id, j.id, j.name, jo.count, jo.totalAmount, jo.payerName, jo.payerPhone, jo.createdAt, jo.reservation, jo.joyPaymentStatus) from JoyOrder jo join jo.joy j on jo.users.id = :userId")
    Page<ResJoyOrderDto> findByUserId(Long userId, Pageable pageable);
}
