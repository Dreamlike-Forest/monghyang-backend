package com.example.monghyang.domain.orders.item.repository;

import com.example.monghyang.domain.orders.item.entity.OrderItemFulfillmentHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderItemFulfillmentHistoryRepository extends JpaRepository<OrderItemFulfillmentHistory, Long> {
    @Query("select h from OrderItemFulfillmentHistory h join h.orderItem oi join oi.orders o join o.user u where h.orderItem.id = :orderItemId and u.id = :userId")
    List<OrderItemFulfillmentHistory> findByOrderItemIdAndUserId(@Param("orderItemId") Long orderItemId, @Param("userId") Long userId);
}
