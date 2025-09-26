package com.example.monghyang.domain.orders.repository;

import com.example.monghyang.domain.orders.entity.Orders;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrdersRepository extends JpaRepository<Orders,Long> {
}
