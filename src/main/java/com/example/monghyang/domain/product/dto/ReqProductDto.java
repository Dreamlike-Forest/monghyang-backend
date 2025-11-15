package com.example.monghyang.domain.product.dto;

import com.example.monghyang.domain.image.dto.AddImageDto;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class ReqProductDto {
    @NotBlank(message = "상품명을 입력해주세요.")
    private String name;
    @NotNull(message = "상품의 알코올 도수를 입력해주세요.")
    private Double alcohol;
    @NotNull(message = "온라인 판매 여부를 입력해주세요.")
    private Boolean is_online_sell;
    @NotNull(message = "상품의 용량을 입력해주세요.")
    private Integer volume;
    @NotNull(message = "상품의 정가를 입력해주세요.")
    @Digits(integer = 8, fraction = 0, message = "가격 정보를 정수로 입력해주세요.")
    private BigDecimal origin_price;
    @Pattern(regexp = "^[^\\s].*\\S.*$", message = "빈 문자열이나 공백만 입력할 수 없습니다")
    private String description;
    private List<AddImageDto> images; // 새로 추가할 이미지 리스트(seq가 1인 이미지가 대표 이미지)
}
