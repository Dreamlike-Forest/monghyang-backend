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

    @Builder
    public BreweryWeeklyOpenTime(@NonNull Brewery brewery, @NonNull DayOfWeek dayOfWeek, @NonNull LocalTime openTime, @NonNull LocalTime closeTime, @NonNull LocalDate effectiveDate) {
        this.brewery = brewery;
        this.dayOfWeek = dayOfWeek;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.effectiveDate = effectiveDate;
    }
}
