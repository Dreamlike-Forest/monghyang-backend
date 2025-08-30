package com.example.monghyang.domain.brewery.main.entity;

import com.example.monghyang.domain.brewery.main.dto.ReqBreweryDto;
import com.example.monghyang.domain.global.advice.ApplicationError;
import com.example.monghyang.domain.global.advice.ApplicationException;
import com.example.monghyang.domain.tag.entity.Tags;
import com.example.monghyang.domain.users.entity.Users;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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

    @Column(columnDefinition = "TEXT")
    private String introduction;
    private String breweryWebsite;

    @Column(nullable = false)
    private Integer minJoyPrice = 0;
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

    @OneToMany(mappedBy = "")
    private List<Tags> tagsList = new ArrayList<>();


    @Builder(builderMethodName = "breweryBuilder")
    public Brewery(@NonNull Users user, @NonNull RegionType regionType, @NonNull String breweryName, @NonNull String breweryAddress, @NonNull String breweryAddressDetail, @NonNull String businessRegistrationNumber, @NonNull String breweryDepositor, @NonNull String breweryAccountNumber, @NonNull String breweryBankName, String introduction, String breweryWebsite, @NonNull Boolean isRegularVisit, @NonNull Boolean isAgreedBrewery) {
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

    public void updateBrewery(ReqBreweryDto reqBreweryDto) {
        if(reqBreweryDto.getBrewery_name() != null){
            this.breweryName = reqBreweryDto.getBrewery_name();
            this.user.updateNickname(reqBreweryDto.getBrewery_name()); // 회원 테이블에도 같이 반영
        }
        if(reqBreweryDto.getBrewery_address() != null){
            this.breweryAddress = reqBreweryDto.getBrewery_address();
            this.user.updateAddress(reqBreweryDto.getBrewery_address()); // 회원 테이블에도 같이 반영
        }
        if(reqBreweryDto.getBrewery_address_detail() != null){
            this.breweryAddressDetail = reqBreweryDto.getBrewery_address_detail();
            this.user.updateAddressDetail(reqBreweryDto.getBrewery_address_detail()); // 회원 테이블에도 같이 반영
        }
        if(reqBreweryDto.getBusiness_registration_number() != null){
            this.businessRegistrationNumber = reqBreweryDto.getBusiness_registration_number();
        }
        if(reqBreweryDto.getBrewery_depositor() != null){
            this.breweryDepositor = reqBreweryDto.getBrewery_depositor();
        }
        if(reqBreweryDto.getBrewery_account_number() != null){
            this.breweryAccountNumber = reqBreweryDto.getBrewery_account_number();
        }
        if(reqBreweryDto.getBrewery_bank_name() != null){
            this.breweryBankName = reqBreweryDto.getBrewery_bank_name();
        }
        if(reqBreweryDto.getIntroduction() != null){
            this.introduction = reqBreweryDto.getIntroduction();
        }
        if(reqBreweryDto.getBrewery_website() != null){
            this.breweryWebsite = reqBreweryDto.getBrewery_website();
        }
        if(reqBreweryDto.getIs_regular_visit() != null){
            this.isRegularVisit = reqBreweryDto.getIs_regular_visit();
        }
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

    public void updateBreweryName(String breweryName) {
        this.breweryName = breweryName;
    }

    public void updateBreweryAddress(String breweryAddress) {
        this.breweryAddress = breweryAddress;
    }

    public void updateBreweryAddressDetail(String breweryAddressDetail) {
        this.breweryAddressDetail = breweryAddressDetail;
    }

    public void updateMinJoyPrice(Integer minJoyPrice) {
        this.minJoyPrice = minJoyPrice;
    }

    public void increaseJoyCount() {
        this.joyCount++;
    }

    public void decreaseJoyCount() {
        this.joyCount--;
    }
}
