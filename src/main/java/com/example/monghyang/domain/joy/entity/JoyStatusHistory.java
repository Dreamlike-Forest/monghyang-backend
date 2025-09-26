package com.example.monghyang.domain.joy.entity;

import com.example.monghyang.domain.users.entity.Users;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JoyStatusHistory {
    @Id @GeneratedValue
    private Long id;
    @JoinColumn(name = "JOY_ORDER_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private JoyOrder joyOrder;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private JoyPaymentStatus toStatus;
    @Column(nullable = false)
    private String reasonCode;
    @Column(nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    private JoyStatusHistory(JoyOrder joyOrder, JoyPaymentStatus toStatus, String reasonCode) {
        this.joyOrder = joyOrder;
        this.toStatus = toStatus;
        this.reasonCode = reasonCode;
    }

    public static JoyStatusHistory joyOrderToStatusReasonCodeOf(@NonNull JoyOrder joyOrder, @NonNull JoyPaymentStatus toStatus, @NonNull String reasonCode) {
        return new JoyStatusHistory(joyOrder, toStatus, reasonCode);
    }
}
