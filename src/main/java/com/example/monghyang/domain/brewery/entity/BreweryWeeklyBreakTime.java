package com.example.monghyang.domain.brewery.entity;

import com.example.monghyang.domain.global.DayOfWeek;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Check;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Check(constraints = "break_start < break_end")
@Table(uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_brewery_break_week",
                columnNames = {"brewery_id", "effective_date", "day_of_week"}
        )
})
public class BreweryWeeklyBreakTime {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @JoinColumn(name = "brewery_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Brewery brewery;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DayOfWeek dayOfWeek;
    @Column(nullable = false)
    private LocalTime breakStart;
    @Column(nullable = false)
    private LocalTime breakEnd;
    @Column(nullable = false)
    private LocalDate effectiveDate;

    /**
     * 양조장 요일별 정기 휴게시간
     * @param brewery 양조장 엔티티
     * @param dayOfWeek 요일 정보
     * @param breakStart 휴게 시작 시간
     * @param breakEnd 휴게 종료 시간
     * @param effectiveDate 적용 시작일
     */
    @Builder
    public BreweryWeeklyBreakTime(Brewery brewery, DayOfWeek dayOfWeek, LocalTime breakStart, LocalTime breakEnd, LocalDate effectiveDate) {
        this.brewery = brewery;
        this.dayOfWeek = dayOfWeek;
        this.breakStart = breakStart;
        this.breakEnd = breakEnd;
        this.effectiveDate = effectiveDate;
    }
}
