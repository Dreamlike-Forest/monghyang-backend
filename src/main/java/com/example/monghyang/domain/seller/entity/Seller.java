package com.example.monghyang.domain.seller.entity;

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
public class Seller {
    @Id @GeneratedValue
    private Long id;
    @JoinColumn(name = "USER_ID", nullable = false)
    @OneToOne(fetch = FetchType.LAZY)
    private Users user;
    @Column(nullable = false)
    private String sellerName;
    @Column(nullable = false)
    private String sellerPhone;
    @Column(nullable = false)
    private String sellerEmail;
    @Column(nullable = false)
    private String sellerAddress;
    @Column(nullable = false)
    private String sellerAddressDetail;
    @CreationTimestamp
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
    public Seller(Users user, String sellerName, String sellerPhone, String sellerEmail, String sellerAddress, String sellerAddressDetail, String businessRegistrationNumber, String sellerAccountNumber, String sellerDepositor, String sellerBankName, String introduction, Boolean isAgreedSeller) {
        if(isAgreedSeller == Boolean.FALSE) {
            // 판매자 약관에 동의하지 않으면 회원 가입 불가
            throw new ApplicationException(ApplicationError.TERMS_AND_CONDITIONS_NOT_AGREED);
        }
        this.user = user;
        this.sellerName = sellerName;
        this.sellerPhone = sellerPhone;
        this.sellerEmail = sellerEmail;
        this.sellerAddress = sellerAddress;
        this.sellerAddressDetail = sellerAddressDetail;
        this.businessRegistrationNumber = businessRegistrationNumber;
        this.sellerAccountNumber = sellerAccountNumber;
        this.sellerDepositor = sellerDepositor;
        this.sellerBankName = sellerBankName;
        this.introduction = introduction;
        this.isAgreedSeller = isAgreedSeller;
    }
}
