package com.example.monghyang.domain.orders.entity;

import com.example.monghyang.domain.users.entity.Users;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Orders {
    @Id @GeneratedValue
    private Long id;
    @JoinColumn(name = "USER_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Users user;
    @Column(nullable = false, precision = 16, scale = 2)
    private BigDecimal totalPrice;
    @Column(nullable = false)
    private String currency;
    @Column(unique = true)
    private String pgPaymentKey;
    @Column(nullable = false, unique = true, updatable = false)
    private UUID pgOrderId;
    @Column(nullable = false)
    private String payerName;
    @Column(nullable = false)
    private String payerPhone;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus;
    @Column(nullable = false)
    private String address;
    @Column(nullable = false)
    private String addressDetail;
    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @Version
    private Long version;
    @Column(nullable = false, columnDefinition = "TINYINT(1)")
    private Boolean isDeleted = Boolean.FALSE;

    public Orders(Users user, BigDecimal totalPrice, String currency, String pgOrderId, String payerName, String payerPhone, PaymentStatus paymentStatus, String address, String addressDetail) {
        this.user = user;
        this.totalPrice = totalPrice;
        this.currency = currency;
        this.pgPaymentKey = pgOrderId;
        this.payerName = payerName;
        this.payerPhone = payerPhone;
        this.paymentStatus = paymentStatus;
        this.address = address;
        this.addressDetail = addressDetail;
    }
}
