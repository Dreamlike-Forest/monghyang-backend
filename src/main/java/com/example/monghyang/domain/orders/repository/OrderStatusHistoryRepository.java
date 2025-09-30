package com.example.monghyang.domain.orders.repository;

import com.example.monghyang.domain.orders.entity.OrderStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, Long> {
    @Query("select osh from OrderStatusHistory osh where osh.order.id = :orderId")
    List<OrderStatusHistory> findByOrderId(@Param("orderId") Long orderId);
}
