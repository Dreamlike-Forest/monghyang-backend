package com.example.monghyang.domain.joy.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class ReqJoyDto {
    @NotBlank(message = "체험의 이름 정보를 입력해주세요.")
    private String name;
    @NotBlank(message = "체험의 장소 정보를 입력해주세요.")
    private String place;
    @NotBlank(message = "체험의 설명 등 상세 정보를 입력해주세요.")
    private String detail;
    @NotNull(message = "체험의 시간 단위를 분 단위로 입력해주세요.")
    @Min(value = 0, message = "체험 시간 단위는 음수가 될 수 없습니다.")
    private Integer time_unit;
    @NotNull(message = "체험의 1인 당 정가 정보를 입력해주세요.")
    @Digits(integer = 8, fraction = 0, message = "가격 정보를 1억 미만의 정수로 입력해주세요.")
    @Min(value = 0, message = "정가는 음수가 될 수 없습니다.")
    private BigDecimal origin_price;
    @NotNull(message = "해당 체험의 동시간 최대 수용 가능 인원 수를 입력해주세요.")
    @Min(value = 1, message = "최대 수용 가능 인원 수는 1 이상이어야 합니다.")
    private Integer max_count;
    /** 체험 생성 시 최초로 저장할 요일별 시작 시간 스냅샷입니다. */
    @Valid
    @NotEmpty(message = "체험 일정 목록을 입력해주세요.")
    private List<JoyScheduleDto> schedules;
    private MultipartFile image;
}
