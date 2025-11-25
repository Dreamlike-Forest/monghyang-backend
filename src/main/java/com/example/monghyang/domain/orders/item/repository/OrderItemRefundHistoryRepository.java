package com.example.monghyang.domain.orders.item.repository;

import com.example.monghyang.domain.orders.item.entity.OrderItemRefundHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderItemRefundHistoryRepository extends JpaRepository<OrderItemRefundHistory, Long> {
    /**
     * 특정 유저의 order item의 refund status history 조회
     * @param orderItemId
     * @param userId
     * @return
     */
    @Query("select h from OrderItemRefundHistory h join h.orderItem oi join oi.orders o where h.orderItem.id = :orderItemId and o.user.id = :userId")
    List<OrderItemRefundHistory> findByOrderItemIdAndUserId(Long orderItemId, Long userId);

    /**
     * 특정 판매자/양조장의 order item의 refund status history 조회
     * @param orderItemIdList order_item_id 리스트
     * @param providerId 판매자/양조자의 회원 식별자
     * @return
     */
    @Query("select h from OrderItemRefundHistory h join h.orderItem oi where h.orderItem.id in :orderItemIdList and oi.provider.id = :providerId")
    List<OrderItemRefundHistory> findByOrderItemIdListAndProviderId(@Param("orderItemIdList") List<Long> orderItemIdList, @Param("providerId") Long providerId);
}
