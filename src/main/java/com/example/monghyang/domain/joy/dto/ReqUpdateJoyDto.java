package com.example.monghyang.domain.joy.dto;

import com.example.monghyang.domain.global.annotation.validation.AllowNullNotBlankString;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@Getter
@Setter
public class ReqUpdateJoyDto {
    @NotNull(message = "수정하려는 체험의 식별자 정보를 입력해주세요.")
    private Long id;
    @Digits(integer = 3, fraction = 1, message = "할인율을 xx.y 형식의 소수로 입력해주세요.")
    @Min(value = 0, message = "할인율은 음수가 될 수 없습니다.")
    private BigDecimal discount_rate;
    @AllowNullNotBlankString
    private String name;
    @AllowNullNotBlankString
    private String place;
    @AllowNullNotBlankString
    private String detail;
    @Min(value = 0, message = "체험 시간 단위는 음수가 될 수 없습니다.")
    private Integer time_unit;
    @Digits(integer = 8, fraction = 0, message = "가격 정보를 100억 미만의 정수로 입력해주세요.")
    @Min(value = 0, message = "정가는 음수가 될 수 없습니다.")
    private BigDecimal origin_price;
    private MultipartFile image; // 새 체험 이미지
    private Boolean is_soldout;
}
