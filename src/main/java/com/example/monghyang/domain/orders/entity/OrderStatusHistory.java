package com.example.monghyang.domain.orders.entity;

import com.example.monghyang.domain.users.entity.Users;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
    @JoinColumn(name = "USER_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private Users user;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus fromStatus;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus toStatus;
    @Column(nullable = false)
    private String reason_code;
    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Builder(builderMethodName = "systemCreate")
    public OrderStatusHistory(Orders order, PaymentStatus fromStatus, PaymentStatus toStatus, String reason_code) {
        // 시스템에서 생성되는 상태 변경
        this.order = order;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.reason_code = reason_code;
    }

    @Builder(builderMethodName = "userCreate")
    public OrderStatusHistory(Orders order, Users user, PaymentStatus fromStatus, PaymentStatus toStatus, String reason_code) {
        // 특정 유저가 요청한 상태 변경
        this.order = order;
        this.user = user;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.reason_code = reason_code;
    }
}
