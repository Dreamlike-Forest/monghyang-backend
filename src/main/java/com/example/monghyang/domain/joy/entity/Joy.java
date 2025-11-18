package com.example.monghyang.domain.joy.entity;

import com.example.monghyang.domain.brewery.entity.Brewery;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Joy { // 양조장 체험
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "BREWERY_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Brewery brewery;

    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String place;
    @Column(nullable = false)
    private String detail;
    @Column(nullable = false, precision = 8)
    private BigDecimal originPrice;
    @Column(nullable = false, precision = 3, scale = 1)
    private BigDecimal discountRate;
    @Column(nullable = false, precision = 8)
    private BigDecimal finalPrice;
    @Column(nullable = false)
    private Integer timeUnit;
    @Column(nullable = false)
    private Integer salesVolume;
    private String imageKey;
    @Column(nullable = false, columnDefinition = "TINYINT(1)")
    private Boolean isSoldout = Boolean.FALSE;
    @Column(nullable = false, columnDefinition = "TINYINT(1)")
    private Boolean isDeleted = Boolean.FALSE;

    @Builder(builderMethodName = "joyBuilder")
    public Joy(@NonNull Brewery brewery, @NonNull String name, @NonNull String place, @NonNull String detail, @NonNull BigDecimal originPrice, @NonNull Integer timeUnit, String imageKey) {
        this.brewery = brewery;
        this.name = name;
        this.place = place;
        this.detail = detail;
        this.originPrice = originPrice;
        this.discountRate = BigDecimal.ZERO; // 초기 할인율 기본값: 0%
        this.finalPrice = originPrice;
        this.salesVolume = 0; // 초기 판매(예약)량: 0
        this.timeUnit = timeUnit;
        this.imageKey = imageKey;
    }

    // 정가와 할인비율 값을 받아 최종 판매가를 계산하는 메소드
    public BigDecimal calFinalPrice(BigDecimal originPrice, BigDecimal discountRate) {
        return originPrice.subtract((originPrice.multiply(discountRate).divide(BigDecimal.valueOf(100.0), 1, RoundingMode.UP)));
    }

    public void setDeleted() {
        this.isDeleted = Boolean.TRUE;
    }

    public void unSetDeleted() {
        this.isDeleted = Boolean.FALSE;
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void updatePlace(String place) {
        this.place = place;
    }

    public void updateDetail(String detail) {
        this.detail = detail;
    }

    public void updateOriginPrice(BigDecimal originPrice) {
        this.originPrice = originPrice;
        this.finalPrice = calFinalPrice(originPrice, this.discountRate);
        // 양조장 최소 체험 가격 갱신 여부 검증 후 갱신
        if(this.finalPrice.compareTo(this.brewery.getMinJoyPrice()) < 0) {
            this.brewery.updateMinJoyPrice(this.finalPrice);
        }
    }

    public void updateDiscountRate(BigDecimal discountRate) {
        this.discountRate = discountRate;
        this.finalPrice = calFinalPrice(this.originPrice, discountRate);
    }

    public void updateSoldout(Boolean isSoldout) {
        this.isSoldout = isSoldout;
    }

    public void updateImageKey(String imageKey) {
        this.imageKey = imageKey;
    }

    public void updateTimeUnit(Integer timeUnit) {
        this.timeUnit = timeUnit;
    }
}
