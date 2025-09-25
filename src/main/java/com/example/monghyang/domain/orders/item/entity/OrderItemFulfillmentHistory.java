package com.example.monghyang.domain.orders.item.entity;

import com.example.monghyang.domain.users.entity.Users;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
    @JoinColumn(name = "USER_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Users users;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private FulfillmentStatus fromStatus;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private FulfillmentStatus toStatus;
    @Column(nullable = false)
    private String reasonCode;
    @Column(nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

}
