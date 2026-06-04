package com.example.monghyang.domain.product.dto;

import com.example.monghyang.domain.global.annotation.validation.AllowNullNotBlankString;
import com.example.monghyang.domain.image.dto.AddImageDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Schema(description = "상품 추가 요청 정보")
public class ReqProductDto {
    @Schema(description = "상품명입니다.", example = "전통주 선물세트", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "상품명을 입력해주세요.")
    private String name;
    @Schema(description = "상품 알코올 도수입니다.", example = "13.5", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "상품의 알코올 도수를 입력해주세요.")
    private Double alcohol;
    @Schema(description = "온라인 판매 여부입니다.", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "온라인 판매 여부를 입력해주세요.")
    private Boolean is_online_sell;
    @Schema(description = "상품 용량입니다. ml 단위로 전달합니다.", example = "750", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "상품의 용량을 입력해주세요.")
    private Integer volume;
    @Schema(description = "상품 초기 재고 수량입니다. 1 이상이어야 합니다.", example = "100", minimum = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "상품의 초기 재고 수량을 입력해주세요.")
    @Min(value = 1, message = "재고 수량은 0보다 커야합니다.")
    private Integer inventory;
    @Schema(description = "상품 정가입니다. 소수점 없는 8자리 이하 정수만 허용됩니다.", example = "30000", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "상품의 정가를 입력해주세요.")
    @Digits(integer = 8, fraction = 0, message = "가격 정보를 정수로 입력해주세요.")
    private BigDecimal origin_price;
    @Schema(description = "상품 설명입니다. null은 허용하지만 빈 문자열은 허용하지 않습니다.", example = "전통주 2병으로 구성된 선물세트입니다.", nullable = true)
    @AllowNullNotBlankString
    private String description;
    @Schema(description = "상품 이미지 목록입니다. seq=1인 이미지가 대표 이미지입니다.")
    private List<AddImageDto> images; // 새로 추가할 이미지 리스트(seq가 1인 이미지가 대표 이미지)
}
