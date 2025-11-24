package com.example.monghyang.domain.orders.item.repository;

import com.example.monghyang.domain.orders.item.entity.OrderItemFulfillmentHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemFulfillmentHistoryRepository extends JpaRepository<OrderItemFulfillmentHistory, Long> {
    List<OrderItemFulfillmentHistory> findByOrderItemId(Long orderItemId);
}
