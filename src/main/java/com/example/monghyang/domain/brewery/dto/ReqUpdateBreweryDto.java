package com.example.monghyang.domain.brewery.dto;

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
    @Pattern(regexp = "\\S+", message = "빈 문자열이나 공백만 입력할 수 없습니다")
    private String brewery_name; // 회원 엔티티 중복 값
    @Pattern(regexp = "\\S+", message = "빈 문자열이나 공백만 입력할 수 없습니다")
    private String brewery_address; // 회원 엔티티 중복 값
    @Pattern(regexp = "\\S+", message = "빈 문자열이나 공백만 입력할 수 없습니다")
    private String brewery_address_detail; // 회원 엔티티 중복 값
    @Pattern(regexp = "\\S+", message = "빈 문자열이나 공백만 입력할 수 없습니다")
    private String business_registration_number;
    @Pattern(regexp = "\\S+", message = "빈 문자열이나 공백만 입력할 수 없습니다")
    private String brewery_depositor;
    @Pattern(regexp = "\\S+", message = "빈 문자열이나 공백만 입력할 수 없습니다")
    private String brewery_account_number;
    @Pattern(regexp = "\\S+", message = "빈 문자열이나 공백만 입력할 수 없습니다")
    private String brewery_bank_name;
    @Pattern(regexp = "\\S+", message = "빈 문자열이나 공백만 입력할 수 없습니다")
    private String introduction;
    @Pattern(regexp = "\\S+", message = "빈 문자열이나 공백만 입력할 수 없습니다")
    private String brewery_website;
    @Pattern(regexp = "\\S+", message = "빈 문자열이나 공백만 입력할 수 없습니다")
    private Boolean is_regular_visit;
    @Pattern(regexp = "\\S+", message = "빈 문자열이나 공백만 입력할 수 없습니다")
    private LocalTime start_time;
    @Pattern(regexp = "\\S+", message = "빈 문자열이나 공백만 입력할 수 없습니다")
    private LocalTime end_time;

    private List<AddImageDto> add_images = new ArrayList<>(); // 새로 추가할 이미지 리스트
    private List<ModifySeqImageDto> modify_images = new ArrayList<>(); // 위치 변경할 이미지 리스트
    private List<Long> remove_images = new ArrayList<>(); // 삭제할 이미지의 식별자 값 리스트
}
