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
public class OrderItemFulfillmentHistory {
    @Id @GeneratedValue
    private Long id;
    @JoinColumn(name = "ORDER_ITEM_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private OrderItem orderItem;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private FulfillmentStatus toStatus;
    @Column(nullable = false)
    private String reasonCode;
    @Column(nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    private OrderItemFulfillmentHistory(OrderItem orderItem, FulfillmentStatus toStatus, String reasonCode) {
        this.orderItem = orderItem;
        this.toStatus = toStatus;
        this.reasonCode = reasonCode;
    }

    public static OrderItemFulfillmentHistory orderItemToStatusReasonCodeOf(@NonNull OrderItem orderItem, @NonNull FulfillmentStatus toStatus, @NonNull String reasonCode) {
        return new OrderItemFulfillmentHistory(orderItem, toStatus, reasonCode);
    }
}
