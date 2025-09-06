package com.example.monghyang.domain.seller.entity;

import com.example.monghyang.domain.global.advice.ApplicationError;
import com.example.monghyang.domain.global.advice.ApplicationException;
import com.example.monghyang.domain.users.entity.Users;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Seller {
    @Id @GeneratedValue
    private Long id;
    @JoinColumn(name = "USER_ID", nullable = false)
    @OneToOne(fetch = FetchType.LAZY)
    private Users user;
    @Column(nullable = false)
    private String sellerName;
    @Column(nullable = false)
    private String sellerAddress;
    @Column(nullable = false)
    private String sellerAddressDetail;
    @CreationTimestamp
    @Column(nullable = false)
    private LocalDate registeredAt;
    @Column(nullable = false)
    private String businessRegistrationNumber;
    @Column(nullable = false)
    private String sellerAccountNumber;
    @Column(nullable = false)
    private String sellerDepositor;
    @Column(nullable = false)
    private String sellerBankName;
    @Column(columnDefinition = "TEXT")
    private String introduction;
    @Column(columnDefinition = "TINYINT(1)", nullable = false)
    private Boolean isAgreedSeller;
    @Column(columnDefinition = "TINYINT(1)", nullable = false)
    private Boolean isDeleted = Boolean.FALSE;

    @Builder(builderMethodName = "sellerBuilder")
    public Seller(@NonNull Users user, @NonNull String sellerName, @NonNull String sellerAddress, @NonNull String sellerAddressDetail, @NonNull String businessRegistrationNumber, @NonNull String sellerAccountNumber, @NonNull String sellerDepositor, @NonNull String sellerBankName, String introduction, @NonNull Boolean isAgreedSeller) {
        if(isAgreedSeller == Boolean.FALSE) {
            // 판매자 약관에 동의하지 않으면 회원 가입 불가
            throw new ApplicationException(ApplicationError.TERMS_AND_CONDITIONS_NOT_AGREED);
        }
        this.user = user;
        this.sellerName = sellerName;
        this.sellerAddress = sellerAddress;
        this.sellerAddressDetail = sellerAddressDetail;
        this.businessRegistrationNumber = businessRegistrationNumber;
        this.sellerAccountNumber = sellerAccountNumber;
        this.sellerDepositor = sellerDepositor;
        this.sellerBankName = sellerBankName;
        this.introduction = introduction;
        this.isAgreedSeller = isAgreedSeller;
    }

    public void setDeleted() {
        // 유저 삭제 처리
        isDeleted = Boolean.TRUE;
    }
    public void unSetDeleted() {
        // 유저 삭제 처리 복구(휴면 회원 복구 등에 사용)
        isDeleted = Boolean.FALSE;
    }

    public void updateSellerName(String sellerName) {
        this.sellerName = sellerName;
        this.user.updateName(sellerName);
    }

    public void updateSellerAddress(String sellerAddress) {
        this.sellerAddress = sellerAddress;
        this.user.updateAddress(sellerAddress);
    }

    public void updateSellerAddressDetail(String sellerAddressDetail) {
        this.sellerAddressDetail = sellerAddressDetail;
        this.user.updateAddressDetail(sellerAddressDetail);
    }

    public void updateBusinessRegistrationNumber(String businessRegistrationNumber) {
        this.businessRegistrationNumber = businessRegistrationNumber;
    }

    public void updateSellerAccountNumber(String sellerAccountNumber) {
        this.sellerAccountNumber = sellerAccountNumber;
    }

    public void updateSellerDepositor(String sellerDepositor) {
        this.sellerDepositor = sellerDepositor;
    }

    public void updateSellerBankName(String sellerBankName) {
        this.sellerBankName = sellerBankName;
    }

    public void updateIntroduction(String introduction) {
        this.introduction = introduction;
    }
}
