package com.example.monghyang.domain.orders.item.repository;

import com.example.monghyang.domain.orders.item.dto.OrderItemDto;
import com.example.monghyang.domain.orders.item.dto.ResOrderItemForSellerDto;
import com.example.monghyang.domain.orders.item.entity.OrderItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
     * @param orderItemId 주문 요소 식별자
     * @param userId 주문자 유저 식별자
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
     * 일반 사용자용: 자신의 주문 식별자 리스트를 통해 주문 요소 조회
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
    where oi.orders.id in :orderIdList and oi.isDeleted = false
    """
    )
    List<OrderItemDto> findByOrderIdList(@Param("orderIdList") List<Long> orderIdList);

    /**
     * 판매자/양조장용: 자신이 판매중인 상품에 대한 모든 주문 요청을 조회(실패한 주문은 제외)
     * @param pageable
     * @param providerId 판매자/양조장 식별자
     * @return
     */
    @Query("""
    select new com.example.monghyang.domain.orders.item.dto.ResOrderItemForSellerDto(
        oi.orders.id, oi.id, p.id, p.name, u.id, u.nickname, u.role,
        pi.imageKey, oi.quantity, oi.amount, oi.fulfillmentStatus, oi.refundStatus,
        oi.carrierCode, oi.trackingNo, oi.shippedAt, oi.deliveredAt, oi.createdAt,
        oi.updatedAt, o.payerName, o.payerPhone, o.address, o.addressDetail
        )
    from OrderItem oi
    join oi.product p
    join oi.provider u
    join oi.orders o
    left join ProductImage pi on pi.product.id = p.id and pi.seq = 1
    where oi.provider.id = :providerId and o.paymentStatus != com.example.monghyang.domain.orders.entity.PaymentStatus.FAILED
    """
    )
    Page<ResOrderItemForSellerDto> findByOrderIdListByProviderId(Pageable pageable, @Param("providerId") Long providerId);
}
