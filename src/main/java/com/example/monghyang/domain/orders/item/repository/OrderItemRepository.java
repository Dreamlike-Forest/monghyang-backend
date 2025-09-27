package com.example.monghyang.domain.orders.item.repository;

import com.example.monghyang.domain.orders.item.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem,Long> {
}
