package com.example.monghyang.domain.seller.dto;

import com.example.monghyang.domain.global.annotation.validation.AllowNullNotBlankString;
import com.example.monghyang.domain.image.dto.AddImageDto;
import com.example.monghyang.domain.image.dto.ModifySeqImageDto;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

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
    private List<AddImageDto> add_images = new ArrayList<>(); // 새로 추가할 이미지 리스트
    private List<ModifySeqImageDto> modify_images = new ArrayList<>(); // 위치 변경할 이미지 리스트
    private List<Long> remove_images = new ArrayList<>(); // 삭제할 이미지의 식별자 값 리스트
}
