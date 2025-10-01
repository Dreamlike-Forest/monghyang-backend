package com.example.monghyang.domain.orders.item.repository;

import com.example.monghyang.domain.orders.item.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem,Long> {
    @Query("select oi from OrderItem oi where oi.orders.id = :orderId")
    List<OrderItem> findByOrderId(@Param("orderId") Long orderId);

    @Query("select oi from OrderItem oi where oi.orders.id in :orderIdList")
    List<OrderItem> findByOrderIdList(@Param("orderIdList") List<Long> orderIdList);
}
