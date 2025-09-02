package com.example.monghyang.domain.brewery.joy.entity;

import com.example.monghyang.domain.brewery.joy.dto.ReqUpdateJoyDto;
import com.example.monghyang.domain.brewery.main.entity.Brewery;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Joy { // 양조장 체험
    @Id @GeneratedValue
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
    @Column(nullable = false)
    private Integer originPrice;
    @Column(nullable = false)
    private Integer discountRate;
    @Column(nullable = false)
    private Integer finalPrice;
    @Column(nullable = false)
    private Integer salesVolume;
    @Column(nullable = false, columnDefinition = "TINYINT(1)")
    private Boolean isSoldout = Boolean.FALSE;
    @Column(nullable = false, columnDefinition = "TINYINT(1)")
    private Boolean isDeleted = Boolean.FALSE;

    @Builder(builderMethodName = "joyBuilder")
    public Joy(@NonNull Brewery brewery, @NonNull String name, @NonNull String place, @NonNull String detail, @NonNull Integer originPrice) {
        this.brewery = brewery;
        this.name = name;
        this.place = place;
        this.detail = detail;
        this.originPrice = originPrice;
        this.discountRate = 0; // 초기 할인율 기본값: 0%
        this.finalPrice = originPrice;
        this.salesVolume = 0; // 초기 판매(예약)량: 0
    }

    // 정가와 할인비율 값을 받아 최종 판매가를 계산하는 메소드
    public Integer calFinalPrice(Integer originPrice, Integer discountRate) {
        return originPrice - (int)(originPrice * discountRate / 100.0);
    }

    public void update(ReqUpdateJoyDto reqUpdateJoyDto) {
        if(reqUpdateJoyDto.getName() != null) {
            this.name = reqUpdateJoyDto.getName();
        }
        if(reqUpdateJoyDto.getPlace() != null) {
            this.place = reqUpdateJoyDto.getPlace();
        }
        if(reqUpdateJoyDto.getDetail() != null) {
            this.detail = reqUpdateJoyDto.getDetail();
        }
        if(reqUpdateJoyDto.getOrigin_price() != null) {
            this.originPrice = reqUpdateJoyDto.getOrigin_price();
            this.finalPrice = calFinalPrice(this.originPrice, this.discountRate);
        }
        if(reqUpdateJoyDto.getDiscount_rate() != null) {
            this.discountRate = reqUpdateJoyDto.getDiscount_rate();
            // 할인율이 반영된 최종 판매자 계산. 정수부만 취급
            this.finalPrice = calFinalPrice(this.originPrice, reqUpdateJoyDto.getDiscount_rate());
        }
        if(reqUpdateJoyDto.getIs_soldout() != null) {
            this.isSoldout = reqUpdateJoyDto.getIs_soldout();
        }
    }

    public void setSoldout() {
        this.isSoldout = Boolean.TRUE;
    }

    public void setDeleted() {
        this.isDeleted = Boolean.TRUE;
    }

    public void unSetSoldout() {
        this.isSoldout = Boolean.FALSE;
    }

    public void unSetDeleted() {
        this.isDeleted = Boolean.FALSE;
    }
}
