package com.example.monghyang.domain.orders.repository;

import com.example.monghyang.domain.orders.entity.OrderStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, Long> {
}
