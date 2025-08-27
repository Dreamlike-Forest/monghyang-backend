package com.example.monghyang.domain.brewery.joy.entity;

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
    public Joy(@NonNull Brewery brewery, @NonNull String name, @NonNull String place, @NonNull String detail, @NonNull Integer originPrice, @NonNull Integer discountRate, @NonNull Integer finalPrice, @NonNull Integer salesVolume) {
        this.brewery = brewery;
        this.name = name;
        this.place = place;
        this.detail = detail;
        this.originPrice = originPrice;
        this.discountRate = discountRate;
        this.finalPrice = finalPrice;
        this.salesVolume = salesVolume;
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
