package com.example.monghyang.domain.brewery.entity;

import com.example.monghyang.domain.global.advice.ApplicationError;
import com.example.monghyang.domain.global.advice.ApplicationException;
import com.example.monghyang.domain.users.entity.Users;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Brewery {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "USER_ID", nullable = false)
    @OneToOne(fetch = FetchType.LAZY)
    private Users user;

    @JoinColumn(name = "REGION_TYPE_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private RegionType regionType;

    @Column(nullable = false)
    private String breweryName; // 상호명
    @Column(nullable = false)
    private String breweryAddress;
    @Column(nullable = false)
    private String breweryAddressDetail;
    @Column(nullable = false)
    private String businessRegistrationNumber;
    @Column(nullable = false)
    private String breweryDepositor;
    @Column(nullable = false)
    private String breweryAccountNumber;
    @Column(nullable = false)
    private String breweryBankName;
    @Column(nullable = false)
    private LocalTime startTime;
    @Column(nullable = false)
    private LocalTime endTime;

    @Column(columnDefinition = "TEXT")
    private String introduction;
    private String breweryWebsite;

    @Column(nullable = false)
    private BigDecimal minJoyPrice = BigDecimal.ZERO;
    @Column(nullable = false)
    private Integer joyCount = 0;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDate registeredAt;
    @Column(columnDefinition = "TINYINT(1)", nullable = false)
    private Boolean isRegularVisit; // 상시 방문 가능 여부
    @Column(columnDefinition = "TINYINT(1)", nullable = false)
    private Boolean isVisitingBrewery = Boolean.FALSE; // '찾아가는 양조장' 여부
    @Column(columnDefinition = "TINYINT(1)", nullable = false)
    private Boolean isAgreedBrewery;
    @Column(columnDefinition = "TINYINT(1)", nullable = false)
    private Boolean isDeleted = Boolean.FALSE;
    
    @Builder(builderMethodName = "breweryBuilder")
    public Brewery(@NonNull Users user, @NonNull RegionType regionType, @NonNull String breweryName, @NonNull String breweryAddress, @NonNull String breweryAddressDetail, @NonNull String businessRegistrationNumber, @NonNull String breweryDepositor, @NonNull String breweryAccountNumber, @NonNull String breweryBankName, String introduction, String breweryWebsite, @NonNull Boolean isRegularVisit, @NonNull Boolean isAgreedBrewery, @NonNull LocalTime startTime, @NonNull LocalTime endTime) {
        if(isAgreedBrewery == Boolean.FALSE){
            throw new ApplicationException(ApplicationError.TERMS_AND_CONDITIONS_NOT_AGREED);
        }
        this.user = user;
        this.regionType = regionType;
        this.breweryName = breweryName;
        this.breweryAddress = breweryAddress;
        this.breweryAddressDetail = breweryAddressDetail;
        this.businessRegistrationNumber = businessRegistrationNumber;
        this.breweryDepositor = breweryDepositor;
        this.breweryAccountNumber = breweryAccountNumber;
        this.breweryBankName = breweryBankName;
        this.introduction = introduction;
        this.breweryWebsite = breweryWebsite;
        this.isRegularVisit = isRegularVisit;
        this.isAgreedBrewery = isAgreedBrewery;
        // 운영시간은 '분' 단위까지만 취급
        this.startTime = startTime.withSecond(0).withNano(0);
        this.endTime = endTime.withSecond(0).withNano(0);
    }

    public void setVisitingBrewery() {
        // '찾아가는 양조장' 으로 변경: 관리자 권한
        isVisitingBrewery = Boolean.TRUE;
    }

    public void unSetVisitingBrewery() {
        // '찾아가는 양조장'을 '일반 양조장'으로 변경: 관리자 권한
        isVisitingBrewery = Boolean.FALSE;
    }

    public void setDeleted() {
        // 유저 삭제 처리
        isDeleted = Boolean.TRUE;
    }
    public void unSetDeleted() {
        // 유저 삭제 처리 복구(휴면 회원 복구 등에 사용)
        isDeleted = Boolean.FALSE;
    }

    public void updateBreweryName(String breweryName) {
        this.breweryName = breweryName;
        this.user.updateName(breweryName); // 회원 테이블에도 같이 반영
    }

    public void updateBreweryAddress(String breweryAddress) {
        this.breweryAddress = breweryAddress;
        this.user.updateAddress(breweryAddress); // 회원 테이블에도 같이 반영
    }

    public void updateBreweryAddressDetail(String breweryAddressDetail) {
        this.breweryAddressDetail = breweryAddressDetail;
        this.user.updateAddressDetail(breweryAddressDetail); // 회원 테이블에도 같이 반영
    }

    public void updateMinJoyPrice(BigDecimal minJoyPrice) {
        this.minJoyPrice = minJoyPrice;
    }

    public void increaseJoyCount() {
        this.joyCount++;
    }

    public void decreaseJoyCount() {
        this.joyCount--;
    }

    public void updateBusinessRegistrationNumber(String businessRegistrationNumber) {
        this.businessRegistrationNumber = businessRegistrationNumber;
    }

    public void updateBreweryDepositor(String breweryDepositor) {
        this.breweryDepositor = breweryDepositor;
    }

    public void updateBreweryAccountNumber(String breweryAccountNumber) {
        this.breweryAccountNumber = breweryAccountNumber;
    }

    public void updateBreweryBankName(String breweryBankName) {
        this.breweryBankName = breweryBankName;
    }

    public void updateIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public void updateBreweryWebsite(String breweryWebsite) {
        this.breweryWebsite = breweryWebsite;
    }

    public void updateRegularVisit(Boolean regularVisit) {
        isRegularVisit = regularVisit;
    }

    public void updateEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public void updateStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }
}
