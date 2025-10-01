package com.example.monghyang.domain.orders.item.repository;

import com.example.monghyang.domain.orders.item.entity.OrderItemRefundHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRefundHistoryRepository extends JpaRepository<OrderItemRefundHistory, Long> {
}
