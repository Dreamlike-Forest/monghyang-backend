package com.example.monghyang.domain.product.entity;

import com.example.monghyang.domain.users.entity.Users;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    @Min(value = 0, message = "재고 수량은 0 미만이 될 수 없습니다.")
    private Integer inventory;
    @Column(nullable = false, precision = 8)
    private BigDecimal originPrice;
    @Column(nullable = false, precision = 3, scale = 1)
    private BigDecimal discountRate;
    @Column(nullable = false, precision = 8)
    private BigDecimal finalPrice;
    @Column(columnDefinition = "TEXT")
    private String description;
    @Column(nullable = false, columnDefinition = "TINYINT(1)")
    private Boolean isSoldout;
    @Column(nullable = false, columnDefinition = "TINYINT(1)")
    private Boolean isDeleted;

    @Builder
    public Product(@NonNull Users user, @NonNull String name, @NonNull Double alcohol, @NonNull Boolean isOnlineSell,
                   @NonNull Integer volume, @NonNull BigDecimal originPrice, @NonNull Integer inventory, String description) {
        this.user = user;
        this.name = name;
        this.alcohol = alcohol;
        this.isOnlineSell = isOnlineSell;
        this.salesVolume = 0;
        this.volume = volume;
        this.inventory = inventory;
        this.originPrice = originPrice;
        this.discountRate = BigDecimal.ZERO;
        this.finalPrice = originPrice;
        this.description = description;
        this.isSoldout = false;
        this.isDeleted = false;
    }

    // 정가와 할인비율 값을 받아 최종 판매가를 계산하는 메소드
    public BigDecimal calFinalPrice(BigDecimal originPrice, BigDecimal discountRate) {
        return originPrice.subtract((originPrice.multiply(discountRate).divide(BigDecimal.valueOf(100.0), 1, RoundingMode.UP)));
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

    public void updateOriginPrice(BigDecimal originPrice) {
        this.originPrice = originPrice;
        this.finalPrice = calFinalPrice(originPrice, this.discountRate);
    }

    public void updateDiscountRate(BigDecimal discountRate) {
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

    public void increaseInventory(Integer increaseNumber) {
        this.inventory += increaseNumber;
    }
    public void decreaseInventory(Integer decreaseNumber) {
        this.inventory -= decreaseNumber;
    }
}