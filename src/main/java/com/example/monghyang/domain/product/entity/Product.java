package com.example.monghyang.domain.product.entity;

import com.example.monghyang.domain.users.entity.Users;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product {
    @Id @GeneratedValue
    private Long id;
    @JoinColumn(name = "USER_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Users user;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private Double alcohol;
    @Column(nullable = false, columnDefinition = "TINYINT(1)")
    private Boolean isOnlineSell;
    @Column(nullable = false)
    private Integer salesVolume;
    @Column(nullable = false)
    @CreationTimestamp
    private LocalDateTime registeredAt;
    @Column(nullable = false)
    private Integer volume;
    @Column(nullable = false)
    private Integer originPrice;
    @Column(nullable = false)
    private Integer discountRate;
    @Column(nullable = false)
    private Integer finalPrice;
    @Column(columnDefinition = "TEXT")
    private String description;
    @Column(nullable = false, columnDefinition = "TINYINT(1)")
    private Boolean isSoldout;
    @Column(nullable = false, columnDefinition = "TINYINT(1)")
    private Boolean isDeleted;

    @Builder
    public Product(Users user, String name, Double alcohol, Boolean isOnlineSell,
                   Integer volume, Integer originPrice, String description) {
        this.user = user;
        this.name = name;
        this.alcohol = alcohol;
        this.isOnlineSell = isOnlineSell;
        this.salesVolume = 0;
        this.volume = volume;
        this.originPrice = originPrice;
        this.discountRate = 0;
        this.finalPrice = originPrice;
        this.description = description;
        this.isSoldout = false;
        this.isDeleted = false;
    }

    // 정가와 할인비율 값을 받아 최종 판매가를 계산하는 메소드
    public Integer calFinalPrice(Integer originPrice, Integer discountRate) {
        return originPrice - (int)(originPrice * discountRate / 100.0);
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void updateAlcohol(Double alcohol) {
        this.alcohol = alcohol;
    }

    public void updateIsOnlineSell(Boolean isOnlineSell) {
        this.isOnlineSell = isOnlineSell;
    }

    public void increaseSalesVolume(Integer increaseNumber) {
        this.salesVolume += increaseNumber;
    }

    public void updateVolume(Integer volume) {
        this.volume = volume;
    }

    public void updateOriginPrice(Integer originPrice) {
        this.originPrice = originPrice;
        this.finalPrice = calFinalPrice(originPrice, this.discountRate);
    }

    public void updateDiscountRate(Integer discountRate) {
        this.discountRate = discountRate;
        this.finalPrice = calFinalPrice(this.originPrice, discountRate);
    }

    public void updateSoldout(Boolean soldout) {
        this.isSoldout = soldout;
    }

    public void setDeleted() {
        this.isDeleted = Boolean.TRUE;
    }

    public void unSetDeleted() {
        this.isDeleted = Boolean.FALSE;
    }

    public void updateDescription(String description) {
        this.description = description;
    }
}