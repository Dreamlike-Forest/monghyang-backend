package com.example.monghyang.domain.orders.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderStatusHistory {
    @Id @GeneratedValue
    private Long id;
    @JoinColumn(name = "ORDER_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Orders order;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus toStatus;
    @Column(nullable = false)
    private String reasonCode;
    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime createdAt;

    private OrderStatusHistory(Orders order, PaymentStatus toStatus, String reasonCode) {
        this.order = order;
        this.toStatus = toStatus;
        this.reasonCode = reasonCode;
    }

    public static OrderStatusHistory orderToStatusReasonCodeOf(@NonNull Orders order, @NonNull PaymentStatus toStatus, @NonNull String reasonCode) {
        return new OrderStatusHistory(order, toStatus, reasonCode);
    }
}
