package com.example.monghyang.domain.orders.entity;

import com.example.monghyang.domain.users.entity.Users;
import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Orders {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @JoinColumn(name = "USER_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Users user;
    @Column(nullable = false, precision = 16, scale = 2)
    @Positive
    private BigDecimal totalAmount;
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
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    @Version
    private Long version;
    @Column(nullable = false, columnDefinition = "TINYINT(1)")
    private Boolean isDeleted = Boolean.FALSE;

    @Builder
    public Orders(@NonNull Users user, @NonNull BigDecimal totalAmount, @NonNull UUID pgOrderId, @NonNull String payerName, @NonNull String payerPhone, @NonNull String address, @NonNull String addressDetail) {
        this.user = user;
        this.totalAmount = totalAmount;
        this.pgOrderId = pgOrderId;
        this.payerName = payerName;
        this.payerPhone = payerPhone;
        this.paymentStatus = PaymentStatus.PENDING;
        this.address = address;
        this.addressDetail = addressDetail;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
    public void setPaid() {
        this.paymentStatus = PaymentStatus.PAID;
    }
    public void setFailed() {
        this.paymentStatus = PaymentStatus.FAILED;
    }

    public void setDeleted() {
        this.isDeleted = true;
    }
    public void setCanceled() {
        this.paymentStatus = PaymentStatus.CANCELED;
    }
}
