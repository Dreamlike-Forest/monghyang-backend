package com.example.monghyang.domain.joy.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.time.LocalTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_joy_closed_start_time",
                columnNames = {"joy_closed_date_id", "closed_start_time"}
        )
})
public class JoyClosedStartTime {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @JoinColumn(name = "joy_closed_date_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private JoyClosedDate joyClosedDate;
    @Column(nullable = false)
    private LocalTime closedStartTime;
    private String reason;
    private JoyClosedStartTime(JoyClosedDate joyClosedDate, LocalTime closedStartTime, String reason) {
        this.joyClosedDate = joyClosedDate;
        this.closedStartTime = closedStartTime;
        this.reason = reason;
    }

    /**
     * 체험의 특정 날짜의 별도 휴무 시간대(시작 시간대)
     * @param joyClosedDate 체험 특정 날짜 별도 휴무 엔티티
     * @param closedStartTime 휴무 시작 시간대
     * @param reason 휴무 사유(필수X)
     * @return
     */
    public JoyClosedStartTime joyClosedDateClosedStartTimeReasonOf(@NonNull JoyClosedDate joyClosedDate, @NonNull LocalTime closedStartTime, String reason) {
        return new JoyClosedStartTime(joyClosedDate, closedStartTime, reason);
    }
}
