package com.example.monghyang.domain.product.dto;

import com.example.monghyang.domain.global.annotation.validation.AllowNullNotBlankString;
import com.example.monghyang.domain.image.dto.AddImageDto;
import com.example.monghyang.domain.image.dto.ModifySeqImageDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Schema(description = "상품 수정 요청 정보")
public class UpdateProductDto {
    @Schema(description = "수정할 상품 식별자입니다.", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "수정하려는 상품의 식별자 정보를 입력해주세요.")
    private Long id;
    @Schema(description = "변경할 상품명입니다. null이면 변경하지 않습니다.", example = "전통주 선물세트", nullable = true)
    @AllowNullNotBlankString
    private String name;
    @Schema(description = "변경할 알코올 도수입니다. null이면 변경하지 않습니다.", example = "13.5", nullable = true)
    private Double alcohol;
    @Schema(description = "변경할 온라인 판매 여부입니다. null이면 변경하지 않습니다.", example = "true", nullable = true)
    private Boolean is_online_sell;
    @Schema(description = "변경할 상품 용량입니다. ml 단위이며 null이면 변경하지 않습니다.", example = "750", minimum = "1", nullable = true)
    @Min(value = 1, message = "술 용량은 0보다 커야 합니다.")
    private Integer volume; // 술 용량
    @Schema(description = "변경할 상품 정가입니다. null이면 변경하지 않습니다.", example = "30000", nullable = true)
    @Digits(integer = 8, fraction = 0, message = "가격 정보를 정수로 입력해주세요.")
    private BigDecimal origin_price;
    @Schema(description = "변경할 할인율입니다. xx.y 형식의 소수로 전달하며 null이면 변경하지 않습니다.", example = "10.0", nullable = true)
    @Digits(integer = 3, fraction = 1, message = "할인율을 xx.y 형식의 소수로 입력해주세요.")
    private BigDecimal discount_rate;
    @Schema(description = "변경할 품절 여부입니다. null이면 변경하지 않습니다.", example = "false", nullable = true)
    private Boolean is_soldout;
    @Schema(description = "변경할 상품 설명입니다. null이면 변경하지 않고 빈 문자열은 허용하지 않습니다.", example = "전통주 2병으로 구성된 선물세트입니다.", nullable = true)
    @AllowNullNotBlankString
    private String description;
    @Schema(description = "새로 추가할 상품 이미지 목록입니다.")
    private List<AddImageDto> add_images = new ArrayList<>(); // 새로 추가할 이미지 리스트
    @Schema(description = "기존 상품 이미지의 노출 순서 변경 목록입니다.")
    private List<ModifySeqImageDto> modify_images = new ArrayList<>(); // 위치 변경할 이미지 리스트
    @Schema(description = "삭제할 기존 상품 이미지 식별자 목록입니다.", example = "[1, 2]")
    private List<Long> remove_images = new ArrayList<>(); // 삭제할 이미지의 식별자 값 리스트
}
