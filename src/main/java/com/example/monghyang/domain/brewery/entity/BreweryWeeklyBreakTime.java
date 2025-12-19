package com.example.monghyang.domain.brewery.entity;

import com.example.monghyang.domain.global.DayOfWeek;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

    @Builder
    public BreweryWeeklyBreakTime(Brewery brewery, DayOfWeek dayOfWeek, LocalTime breakStart, LocalTime breakEnd, LocalDate effectiveDate) {
        this.brewery = brewery;
        this.dayOfWeek = dayOfWeek;
        this.breakStart = breakStart;
        this.breakEnd = breakEnd;
        this.effectiveDate = effectiveDate;
    }
}
