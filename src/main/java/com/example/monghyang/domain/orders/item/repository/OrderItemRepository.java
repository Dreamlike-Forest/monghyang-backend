package com.example.monghyang.domain.orders.item.repository;

import com.example.monghyang.domain.orders.item.dto.OrderItemDto;
import com.example.monghyang.domain.orders.item.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderItemRepository extends JpaRepository<OrderItem,Long> {
    @Query("select oi from OrderItem oi where oi.orders.id = :orderId")
    List<OrderItem> findByOrderId(@Param("orderId") Long orderId);

    /**
     * PAID 상태의 주문의 주문요소를 조회
     * @param orderItemId
     * @param userId
     * @return
     */
    @Query("""
    select oi
    from OrderItem oi
    join oi.orders o
    where oi.id = :orderItemId
    and o.user.id = :userId
    and o.paymentStatus = com.example.monghyang.domain.orders.entity.PaymentStatus.PAID
    """)
    Optional<OrderItem> findByPaidOrderItemIdAndUserId(@Param("orderItemId") Long orderItemId, @Param("userId") Long userId);

    @Query("select oi from OrderItem oi join oi.orders o where oi.id = :orderItemId and o.user.id = :userId")
    Optional<OrderItem> findByOrderItemIdAndUserId(@Param("orderItemId") Long orderItemId, @Param("userId") Long userId);

    /**
     * 해당 주문의 '취소되지 않은' 주문 요소의 숫자 카운트
     * @param orderId 주문 식별자
     * @return
     */
    @Query("select count(*) from OrderItem oi where oi.orders.id = :orderId and oi.fulfillmentStatus != com.example.monghyang.domain.orders.item.entity.FulfillmentStatus.CANCELED")
    Integer countNotCanceledOrderItemByOrderId(@Param("orderId") Long orderId);


    /**
     * 주문 식별자 리스트를 통해 주문 요소 조회
     * @param orderIdList 주문 식별자 리스트
     * @return
     */
    @Query("""
    select oi.orders.id as orderId,
        oi.id as orderItemId, p.id as productId, p.name as productName,
        u.id as providerId, u.nickname as providerNickname, u.role as providerRole,
        pi.imageKey as productImageKey, oi.quantity as orderItemQuantity, oi.amount as orderItemAmount,
        oi.fulfillmentStatus as orderItemFulfillmentStatus, oi.refundStatus as orderItemRefundStatus,
        oi.carrierCode as orderItemCarrierCode, oi.trackingNo as orderItemTrackingNo,
        oi.shippedAt as orderItemShippedAt, oi.deliveredAt as orderItemDeliveredAt, oi.createdAt as orderItemCreatedAt,
        oi.updatedAt as orderItemUpdatedAt
    from OrderItem oi
    join oi.product p
    join oi.provider u
    left join ProductImage pi on pi.product.id = p.id and pi.seq = 1
    where oi.orders.id in :orderIdList
    """
    )
    List<OrderItemDto> findByOrderIdList(@Param("orderIdList") List<Long> orderIdList);
}
