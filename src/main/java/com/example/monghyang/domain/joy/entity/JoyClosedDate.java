package com.example.monghyang.domain.joy.entity;

import com.example.monghyang.domain.global.ClosedStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_joy_closed_date",
                columnNames = {"joy_id", "closed_date"}
        )
})
public class JoyClosedDate {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @JoinColumn(name = "joy_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Joy joy;
    @Column(nullable = false)
    private LocalDate closedDate;
    @Column(nullable = false, columnDefinition = "tinyint(1)")
    private Boolean isAllDay;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClosedStatus closedStatus;
    private String reason;

    private JoyClosedDate(Joy joy, LocalDate closedDate, Boolean isAllDay, String reason) {
        this.joy = joy;
        this.closedDate = closedDate;
        this.isAllDay = isAllDay;
        this.reason = reason;
        this.closedStatus = ClosedStatus.PENDING;
    }

    /**
     * 체험 별도 휴무일
     * @param joy 체험 식별자
     * @param closedDate 별도 휴무일 날짜
     * @param isAllDay 하루 전체 휴무 여부(일부 시간대만 휴무 가능)
     * @param reason 휴무 사유(필수 X)
     * @return
     */
    public static JoyClosedDate joyClosedDateIsAllDayReason(@NonNull Joy joy, @NonNull LocalDate closedDate, @NonNull Boolean isAllDay, String reason) {
        return new JoyClosedDate(joy, closedDate, isAllDay, reason);
    }
}
