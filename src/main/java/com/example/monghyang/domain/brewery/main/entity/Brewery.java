package com.example.monghyang.domain.brewery.main.entity;

import com.example.monghyang.domain.global.advice.ApplicationError;
import com.example.monghyang.domain.global.advice.ApplicationException;
import com.example.monghyang.domain.users.entity.Users;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Brewery {
    @Id @GeneratedValue
    private Long id;

    @JoinColumn(name = "USER_ID", nullable = false)
    @OneToOne(fetch = FetchType.LAZY)
    private Users user;

    @JoinColumn(name = "REGION_TYPE_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private RegionType regionType;

    @Column(nullable = false)
    private String breweryName;
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

    @Column(columnDefinition = "TEXT")
    private String introduction;
    private String breweryWebsite;

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
    public Brewery(Users user, RegionType regionType, String breweryName, String breweryAddress, String breweryAddressDetail, String businessRegistrationNumber, String breweryDepositor, String breweryAccountNumber, String breweryBankName, String introduction, String breweryWebsite, Boolean isRegularVisit, Boolean isAgreedBrewery) {
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
    }

    public void setVisitingBrewery() {
        // '찾아가는 양조장' 으로 변경
        isVisitingBrewery = Boolean.TRUE;
    }

    public void unSetVisitingBrewery() {
        // '찾아가는 양조장'을 '일반 양조장'으로 변경
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

    public void updateBreweryAddress(String breweryAddress) {
        this.breweryAddress = breweryAddress;
    }

    public void updateBreweryAddressDetail(String breweryAddressDetail) {
        this.breweryAddressDetail = breweryAddressDetail;
    }
}
