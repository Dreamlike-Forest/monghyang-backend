package com.example.monghyang.domain.joy.entity;

import com.example.monghyang.domain.users.entity.Users;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JoyOrder {
    @Id @GeneratedValue
    private Long id;
    @JoinColumn(name = "USER_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Users users;
    @JoinColumn(name = "JOY_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Joy joy;
    private Integer count;
    @Column(nullable = false, precision = 12, scale = 2)
    @Positive
    private BigDecimal totalPrice;
    @Column(unique = true)
    private String pgPaymentKey; // pg사에서 발급받은 key. 실제 결제 식별에 사용됨.
    @Column(nullable = false, unique = true, updatable = false)
    private UUID pgOrderId;
    @Column(nullable = false)
    private String payerName;
    @Column(nullable = false)
    private String payerPhone;
    @CreationTimestamp
    private LocalDateTime createdAt;
    @Column(nullable = false)
    private LocalDateTime reservation;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private JoyPaymentStatus joyPaymentStatus;
    @Column(nullable = false, columnDefinition = "TINYINT(1)")
    private Boolean isDeleted = false;

    // 주문 시 입력받을 값: 예약 인원수, 예약자명, 예약자 연락처, 체험 시작 시각, 체험 종료 시각
    @Builder
    public JoyOrder(@NonNull Joy joy, @NotNull Users users, @NonNull Integer count, @NonNull UUID pgOrderId, @NonNull String payerName, @NonNull String payerPhone, @NonNull LocalDateTime reservation) {
        this.joy = joy;
        this.users = users;
        this.count = count;
        this.pgOrderId = pgOrderId;
        this.payerName = payerName;
        this.payerPhone = payerPhone;
        this.reservation = reservation.withSecond(0).withNano(0); // 분 단위까지 저장
        this.totalPrice = joy.getFinalPrice().multiply(BigDecimal.valueOf(count)); // 최종 결제 금액
        this.joyPaymentStatus = JoyPaymentStatus.PENDING;
    }

    public void setCanceled() {
        this.joyPaymentStatus = JoyPaymentStatus.CANCELED;
    }
    public void setDeleted() {
        this.isDeleted = true;
    }
    public void unSetDeleted() {
        this.isDeleted = false;
    }

    public void updateReservation(LocalDateTime reservation) {
        this.reservation = reservation.withSecond(0).withNano(0);
    }

    public void setPgPaymentKey(String pgPaymentKey) {
        if(this.pgPaymentKey == null) {
            // 딱 한번만 수정 가능
            this.pgPaymentKey = pgPaymentKey;
        }
    }

    public void setPaid() {
        this.joyPaymentStatus = JoyPaymentStatus.PAID;
    }

    public void setFailed() {
        this.joyPaymentStatus = JoyPaymentStatus.FAILED;
    }
}
