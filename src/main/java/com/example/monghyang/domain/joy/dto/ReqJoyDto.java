package com.example.monghyang.domain.joy.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

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
    private MultipartFile image;
}
