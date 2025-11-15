package com.example.monghyang.domain.seller.dto;

import com.example.monghyang.domain.global.annotation.validation.AllowNullNotBlankString;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqSellerDto {
    @AllowNullNotBlankString
    private String seller_name;
    @AllowNullNotBlankString
    private String seller_address;
    @AllowNullNotBlankString
    private String seller_address_detail;
    @AllowNullNotBlankString
    private String business_registration_number;
    @AllowNullNotBlankString
    private String seller_account_number;
    @AllowNullNotBlankString
    private String seller_depositor;
    @AllowNullNotBlankString
    private String seller_bank_name;
    @AllowNullNotBlankString
    private String introduction;
}
