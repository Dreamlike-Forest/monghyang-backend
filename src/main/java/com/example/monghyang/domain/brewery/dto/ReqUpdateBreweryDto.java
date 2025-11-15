package com.example.monghyang.domain.brewery.dto;

import com.example.monghyang.domain.global.annotation.validation.AllowNullNotBlankString;
import com.example.monghyang.domain.image.dto.AddImageDto;
import com.example.monghyang.domain.image.dto.ModifySeqImageDto;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ReqUpdateBreweryDto {
    @AllowNullNotBlankString
    private String brewery_name; // 회원 엔티티 중복 값
    @AllowNullNotBlankString
    private String brewery_address; // 회원 엔티티 중복 값
    @AllowNullNotBlankString
    private String brewery_address_detail; // 회원 엔티티 중복 값
    @AllowNullNotBlankString
    private String business_registration_number;
    @AllowNullNotBlankString
    private String brewery_depositor;
    @AllowNullNotBlankString
    private String brewery_account_number;
    @AllowNullNotBlankString
    private String brewery_bank_name;
    @AllowNullNotBlankString
    private String introduction;
    @AllowNullNotBlankString
    private String brewery_website;
    private Boolean is_regular_visit;
    private LocalTime start_time;
    private LocalTime end_time;

    private List<AddImageDto> add_images = new ArrayList<>(); // 새로 추가할 이미지 리스트
    private List<ModifySeqImageDto> modify_images = new ArrayList<>(); // 위치 변경할 이미지 리스트
    private List<Long> remove_images = new ArrayList<>(); // 삭제할 이미지의 식별자 값 리스트
}
