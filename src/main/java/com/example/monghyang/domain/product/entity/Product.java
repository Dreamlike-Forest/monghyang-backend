package com.example.monghyang.domain.product.entity;

import com.example.monghyang.domain.users.entity.Users;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
    private String imageKey;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private Double alcohol;
    @Column(nullable = false)
    private Boolean isOnlineSell;
    @Column(nullable = false)
    private Integer salesVolume;
    @Column(nullable = false)
    private LocalDateTime registeredAt;
    @Column(nullable = false)
    private Integer volume;
    @Column(nullable = false)
    private Integer originPrice;
    @Column(nullable = false)
    private Integer discountRate;
    @Column(nullable = false)
    private Integer finalPrice;
    @Column(nullable = false)
    private Boolean isSoldout;
    @Column(nullable = false)
    private Boolean isDeleted;

    @Builder
    public Product(Users user, String imageKey, String name, Double alcohol, Boolean isOnlineSell, Integer salesVolume,
                   Integer volume, Integer originPrice) {
        this.user = user;
        this.imageKey = imageKey;
        this.name = name;
        this.alcohol = alcohol;
        this.isOnlineSell = isOnlineSell;
        this.salesVolume = salesVolume;
        this.volume = volume;
        this.originPrice = originPrice;
        this.discountRate = 0;
        this.finalPrice = originPrice;
        this.isSoldout = false;
        this.isDeleted = false;
    }

    // 정가와 할인비율 값을 받아 최종 판매가를 계산하는 메소드
    public Integer calFinalPrice(Integer originPrice, Integer discountRate) {
        return originPrice - (int)(originPrice * discountRate / 100.0);
    }

    public void updateImageKey(String imageKey) {
        this.imageKey = imageKey;
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

    public void setSoldout(Boolean soldout) {
        this.isSoldout = soldout;
    }

    public void setDeleted() {
        this.isDeleted = Boolean.TRUE;
    }

    public void unSetDeleted() {
        this.isDeleted = Boolean.FALSE;
    }
}