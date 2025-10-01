package com.example.monghyang.domain.orders.repository;

import com.example.monghyang.domain.orders.entity.Orders;
import com.example.monghyang.domain.orders.item.dto.ResOrderListDto;
import com.example.monghyang.domain.users.entity.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface OrdersRepository extends JpaRepository<Orders,Long> {
    @Query("select o from Orders o where o.pgOrderId = :pgOrderId")
    Optional<Orders> findByPgOrderId(@Param("pgOrderId") UUID pgOrderId);

    @Query("select o from Orders o where o.user.id = :userId and o.isDeleted = false")
    Optional<Orders> findByUserId(@Param("userId") Long userId);

    // pending 상태를 제외한 주문 내역만 조회
    @Query("select new com.example.monghyang.domain.orders.item.dto.ResOrderListDto(o.id, o.createdAt) from Orders o where o.user.id = :userId and o.paymentStatus != com.example.monghyang.domain.orders.entity.PaymentStatus.PENDING and o.isDeleted = false")
    Page<ResOrderListDto> findIdByUserIdPaging(@Param("userId") Long userId, Pageable pageable);

    Long user(Users user);
}
