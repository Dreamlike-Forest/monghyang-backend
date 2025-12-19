package com.example.monghyang.domain.brewery.entity;

import com.example.monghyang.domain.global.DayOfWeek;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BreweryWeeklyOpenTime {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @JoinColumn(name = "brewery_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Brewery brewery;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DayOfWeek dayOfWeek;
    @Column(nullable = false)
    private LocalTime openTime;
    @Column(nullable = false)
    private LocalTime closeTime;
    @Column(nullable = false)
    private LocalDate effectiveDate;

    /**
     * 양조장 요일별 정기 운영일
     * @param brewery 양조장 엔티티
     * @param dayOfWeek 요일 정보
     * @param openTime 운영 시작 시간
     * @param closeTime 운영 종료 시간
     * @param effectiveDate 적용 시작일
     */
    @Builder
    public BreweryWeeklyOpenTime(@NonNull Brewery brewery, @NonNull DayOfWeek dayOfWeek, @NonNull LocalTime openTime, @NonNull LocalTime closeTime, @NonNull LocalDate effectiveDate) {
        this.brewery = brewery;
        this.dayOfWeek = dayOfWeek;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.effectiveDate = effectiveDate;
    }
}
