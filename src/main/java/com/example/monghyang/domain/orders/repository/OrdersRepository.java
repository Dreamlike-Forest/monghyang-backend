package com.example.monghyang.domain.orders.repository;

import com.example.monghyang.domain.orders.dto.ResOrderDto;
import com.example.monghyang.domain.orders.entity.Orders;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface OrdersRepository extends JpaRepository<Orders,Long> {
    @Query("select o from Orders o where o.pgOrderId = :pgOrderId and o.paymentStatus = com.example.monghyang.domain.orders.entity.PaymentStatus.PENDING")
    Optional<Orders> findByPgOrderId(@Param("pgOrderId") UUID pgOrderId);

    @Query("select o from Orders o where o.user.id = :userId and o.isDeleted = false")
    Optional<Orders> findByUserId(@Param("userId") Long userId);

    /**
     * pending 상태를 제외한 주문 내역 정보 최신순 조회 페이징
     * @param pageable 페이징 정보
     * @param userId 유저 식별자
     * @return
     */
    @Query("""
    select new com.example.monghyang.domain.orders.dto.ResOrderDto(
        o.id, o.totalAmount, o.currency, o.payerName, o.payerPhone, o.paymentStatus,
            o.address, o.addressDetail, o.createdAt, o.updatedAt
        )
    from Orders o
    where o.user.id = :userId
    and o.paymentStatus != com.example.monghyang.domain.orders.entity.PaymentStatus.PENDING
    and o.isDeleted = false
    order by o.createdAt desc
    """)
    Page<ResOrderDto> findByUserId(Pageable pageable, @Param("userId") Long userId);

    @Query("select o from Orders o where o.pgOrderId = :pgOrderId")
    Optional<Orders> findByPgOrderIdForSetFailed(@Param("pgOrderId") UUID pgOrderId);
}
