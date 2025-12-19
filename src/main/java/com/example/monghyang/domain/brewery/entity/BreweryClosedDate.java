package com.example.monghyang.domain.brewery.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BreweryClosedDate {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @JoinColumn(name = "brewery_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Brewery brewery;
    @Column(nullable = false)
    private LocalDate closedDate;
    private String reason;

    private BreweryClosedDate(Brewery brewery, LocalDate closedDate, String reason) {
        this.brewery = brewery;
        this.closedDate = closedDate;
        this.reason = reason;
    }

    /**
     * reason은 필수가 아닙니다.
     * @param brewery 양조장 엔티티
     * @param closedDate 양조장 별도 휴무일
     * @param reason 휴무 사유
     * @return
     */
    public static BreweryClosedDate breweryClosedDateReasonOf(@NonNull Brewery brewery, @NonNull LocalDate closedDate, String reason) {
        return new BreweryClosedDate(brewery, closedDate, reason);
    }
}
