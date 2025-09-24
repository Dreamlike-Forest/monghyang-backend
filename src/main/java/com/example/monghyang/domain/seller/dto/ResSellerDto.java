package com.example.monghyang.domain.seller.dto;

import com.example.monghyang.domain.seller.entity.Seller;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class ResSellerDto {
    private Long seller_id;
    private String seller_name;
    private String seller_address;
    private String seller_address_detail;
    private LocalDate seller_registered_at;
    private String seller_business_registration_number;
    private String seller_seller_account_number;
    private String seller_seller_depositor;
    private String seller_seller_bank_name;
    private String seller_introduction;

    private ResSellerDto(Seller seller) {
        this.seller_id = seller.getId();
        this.seller_name = seller.getSellerName();
        this.seller_address = seller.getSellerAddress();
        this.seller_address_detail = seller.getSellerAddressDetail();
        this.seller_registered_at = seller.getRegisteredAt();
        this.seller_business_registration_number = seller.getBusinessRegistrationNumber();
        this.seller_seller_account_number = seller.getSellerAccountNumber();
        this.seller_seller_depositor = seller.getSellerDepositor();
        this.seller_seller_bank_name = seller.getSellerBankName();
        this.seller_introduction = seller.getIntroduction();
    }

    public static ResSellerDto sellerFrom(Seller seller) {
        return new ResSellerDto(seller);
    }
}
