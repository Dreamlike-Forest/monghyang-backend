package com.example.monghyang.domain.joy.entity;

import com.example.monghyang.domain.global.DayOfWeek;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_joy_weekly_start_time",
                columnNames = {"joy_id", "effective_date", "day_of_week", "start_time"}
        )
})
public class JoyWeeklyStartTime {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @JoinColumn(name = "joy_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Joy joy;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DayOfWeek dayOfWeek;
    @Column(nullable = false)
    private LocalTime startTime;
    @Column(nullable = false)
    private LocalDate effectiveDate;

    private JoyWeeklyStartTime(Joy joy, DayOfWeek dayOfWeek, LocalTime startTime, LocalDate effectiveDate) {
        this.joy = joy;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.effectiveDate = effectiveDate;
    }

    /**
     * 요일별 체험 시작 시간대
     * @param joy 체험 엔티티
     * @param dayOfWeek 요일 정보
     * @param startTime 시작 시간대
     * @param effectiveDate 적용 시작일
     */
    public static JoyWeeklyStartTime joyDayOfWeekStartTimeEffectiveDateOf(@NonNull Joy joy, @NonNull DayOfWeek dayOfWeek, @NonNull LocalTime startTime, @NonNull LocalDate effectiveDate) {
        return new JoyWeeklyStartTime(joy, dayOfWeek, startTime, effectiveDate);
    }
}
