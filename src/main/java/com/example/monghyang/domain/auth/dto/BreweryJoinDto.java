package com.example.monghyang.domain.auth.dto;

import com.example.monghyang.domain.global.annotation.validation.AllowNullNotBlankString;
import com.example.monghyang.domain.image.dto.AddImageDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class BreweryJoinDto extends JoinDto {
    @NotBlank(message = "business_registration_number 값이 공백일 수 없습니다.")
    private String business_registration_number;
    @NotBlank(message = "brewery_depositor 값이 공백일 수 없습니다.")
    private String brewery_depositor;
    @NotBlank(message = "brewery_account_number 값이 공백일 수 없습니다.")
    private String brewery_account_number;
    @NotBlank(message = "brewery_bank_name 값이 공백일 수 없습니다.")
    private String brewery_bank_name;
    @AllowNullNotBlankString
    private String introduction;
    @AllowNullNotBlankString
    private String brewery_website;
    @Valid
    @NotNull
    @Size(min = 1, message = "양조장 운영 시간대 정보는 최소 1개 이상 입력해야합니다.")
    private List<BreweryScheduleDto> schedules; // 요일 별 운영/휴게시간 정보. 최소 1개 이상 있어야 한다.

    @Size(max = 5, message = "이미지는 최대 5개까지 추가할 수 있습니다.")
    private List<AddImageDto> images; // 새로 추가할 이미지 파일 + 순서 정보 쌍의 리스트

    @NotNull(message = "region_type_id 값이 공백일 수 없습니다.")
    private Integer region_type_id;

    @NotNull(message = "is_regular_visit 값이 공백일 수 없습니다.")
    private Boolean is_regular_visit;
    @NotNull(message = "is_agreed_brewery 값이 공백일 수 없습니다.")
    private Boolean is_agreed_brewery;
}