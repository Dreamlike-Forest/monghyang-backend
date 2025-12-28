package com.example.monghyang.domain.brewery.entity;

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
                name = "uk_brewery_closed_date",
                columnNames = {"brewery_id", "closed_date"}
        )
})
public class BreweryClosedDate {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @JoinColumn(name = "brewery_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Brewery brewery;
    @Column(nullable = false)
    private LocalDate closedDate;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClosedStatus closedStatus;
    private String reason;

    private BreweryClosedDate(Brewery brewery, LocalDate closedDate, String reason) {
        this.brewery = brewery;
        this.closedDate = closedDate;
        this.reason = reason;
        this.closedStatus = ClosedStatus.PENDING; // 생성 시 '보류(검토) 중' 상태로 초기화
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
