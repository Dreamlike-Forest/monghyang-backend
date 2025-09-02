package com.example.monghyang.domain.brewery.main.dto;

import com.example.monghyang.domain.image.dto.AddImageDto;
import com.example.monghyang.domain.image.dto.ModifySeqImageDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ReqBreweryDto {
    private String brewery_name; // 회원 엔티티 중복 값
    private String brewery_address; // 회원 엔티티 중복 값
    private String brewery_address_detail; // 회원 엔티티 중복 값
    private String business_registration_number;
    private String brewery_depositor;
    private String brewery_account_number;
    private String brewery_bank_name;
    private String introduction;
    private String brewery_website;
    private Boolean is_regular_visit;

    private List<AddImageDto> add_images = new ArrayList<>(); // 새로 추가할 이미지 리스트
    private List<ModifySeqImageDto> modify_images = new ArrayList<>(); // 위치 변경할 이미지 리스트
    private List<Long> remove_images = new ArrayList<>(); // 삭제할 이미지의 식별자 값 리스트
}
