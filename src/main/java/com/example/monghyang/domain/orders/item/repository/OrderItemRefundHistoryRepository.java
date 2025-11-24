package com.example.monghyang.domain.orders.item.repository;

import com.example.monghyang.domain.orders.item.entity.OrderItemRefundHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderItemRefundHistoryRepository extends JpaRepository<OrderItemRefundHistory, Long> {
    @Query("select h from OrderItemRefundHistory h join h.orderItem oi join oi.orders o join o.user u where h.orderItem.id = :orderItemId and u.id = :userId")
    List<OrderItemRefundHistory> findByOrderItemIdAndUserId(Long orderItemId, Long userId);
}
