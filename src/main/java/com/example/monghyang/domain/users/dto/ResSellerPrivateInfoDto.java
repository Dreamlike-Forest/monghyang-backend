package com.example.monghyang.domain.users.dto;

import com.example.monghyang.domain.seller.entity.Seller;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class ResSellerPrivateInfoDto {
    private final Long seller_id;
    private final String seller_name;
    private final String seller_address;
    private final String seller_address_detail;
    private final LocalDate seller_registered_at;
    private final String seller_business_registration_number;
    private final String seller_account_number;
    private final String seller_depositor;
    private final String seller_bank_name;
    private final String seller_introduction;
    private final Boolean seller_is_agreed_seller;
    private final Boolean seller_is_deleted;
    public ResSellerPrivateInfoDto(Seller seller) {
        this.seller_id = seller.getId();
        this.seller_name = seller.getSellerName();
        this.seller_address = seller.getSellerAddress();
        this.seller_address_detail = seller.getSellerAddressDetail();
        this.seller_registered_at = seller.getRegisteredAt();
        this.seller_business_registration_number = seller.getBusinessRegistrationNumber();
        this.seller_account_number = seller.getSellerAccountNumber();
        this.seller_depositor = seller.getSellerDepositor();
        this.seller_bank_name = seller.getSellerBankName();
        this.seller_introduction = seller.getIntroduction();
        this.seller_is_agreed_seller = seller.getIsAgreedSeller();
        this.seller_is_deleted = seller.getIsDeleted();
    }

}
