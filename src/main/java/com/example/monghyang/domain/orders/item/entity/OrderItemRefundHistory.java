package com.example.monghyang.domain.orders.item.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItemRefundHistory {
    @Id @GeneratedValue
    private Long id;
    @JoinColumn(name = "ORDER_ITEM_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private OrderItem orderItem;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RefundStatus toStatus;
    @Column(nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    private OrderItemRefundHistory(OrderItem orderItem, RefundStatus toStatus) {
        this.orderItem = orderItem;
        this.toStatus = toStatus;
    }

    public static OrderItemRefundHistory orderItemToStatusOf(@NonNull OrderItem orderItem, @NonNull RefundStatus toStatus) {
        return new OrderItemRefundHistory(orderItem, toStatus);
    }
}
