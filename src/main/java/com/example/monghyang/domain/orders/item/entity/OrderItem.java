package com.example.monghyang.domain.orders.item.entity;

import com.example.monghyang.domain.orders.entity.Orders;
import com.example.monghyang.domain.product.entity.Product;
import com.example.monghyang.domain.users.entity.Users;
import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @JoinColumn(name = "ORDER_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Orders orders;
    @JoinColumn(name = "PRODUCT_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Product product;
    @JoinColumn(name = "PROVIDER_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Users provider;
    @Column(nullable = false)
    @Positive
    private Integer quantity;
    @Column(nullable = false, precision = 12, scale = 2)
    @Positive
    private BigDecimal amount;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private FulfillmentStatus fulfillmentStatus;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RefundStatus refundStatus;
    private String carrierCode; // 택배사 코드
    private String trackingNo; // 운송장 번호
    private LocalDateTime shippedAt; // 실제 발송 시각
    private LocalDateTime deliveredAt; // 배송 완료 시각
    @Column(nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt; // 주문 요소 생성 시각
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    @Version
    private Long version;
    @Column(nullable = false, columnDefinition = "TINYINT(1)")
    private Boolean isDeleted;

    @Builder
    public OrderItem(@NonNull Orders orders, @NonNull Product product, @NonNull Users provider, @NonNull Integer quantity, @NonNull BigDecimal amount) {
        this.orders = orders;
        this.product = product;
        this.provider = provider;
        this.quantity = quantity;
        this.amount = amount;
        this.fulfillmentStatus = FulfillmentStatus.CREATED;
        this.refundStatus = RefundStatus.NONE;
    }

    public void updateFulfillmentStatus(FulfillmentStatus fulfillmentStatus) {
        this.fulfillmentStatus = fulfillmentStatus;
    }
    public void updateRefundStatus(RefundStatus refundStatus) {
        this.refundStatus = refundStatus;
    }
}
