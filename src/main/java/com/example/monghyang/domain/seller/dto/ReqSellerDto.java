package com.example.monghyang.domain.seller.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqSellerDto {
    private String seller_name;
    private String seller_address;
    private String seller_address_detail;
    private String business_registration_number;
    private String seller_account_number;
    private String seller_depositor;
    private String seller_bank_name;
    private String introduction;
}
