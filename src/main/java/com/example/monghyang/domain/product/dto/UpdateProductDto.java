package com.example.monghyang.domain.product.dto;

import com.example.monghyang.domain.image.dto.AddImageDto;
import com.example.monghyang.domain.image.dto.ModifySeqImageDto;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class UpdateProductDto {
    @NotNull(message = "수정하려는 상품의 식별자 정보를 입력해주세요.")
    private Long id;
    @Pattern(regexp = "^[^\\s].*\\S.*$", message = "빈 문자열이나 공백만 입력할 수 없습니다")
    private String name;
    @Pattern(regexp = "^[^\\s].*\\S.*$", message = "빈 문자열이나 공백만 입력할 수 없습니다")
    private Double alcohol;
    @Pattern(regexp = "^[^\\s].*\\S.*$", message = "빈 문자열이나 공백만 입력할 수 없습니다")
    private Boolean is_online_sell;
    @Pattern(regexp = "^[^\\s].*\\S.*$", message = "빈 문자열이나 공백만 입력할 수 없습니다")
    private Integer volume; // 술 용량
    @Digits(integer = 8, fraction = 0, message = "가격 정보를 정수로 입력해주세요.")
    private BigDecimal origin_price;
    @Digits(integer = 3, fraction = 1, message = "할인율을 xx.y 형식의 소수로 입력해주세요.")
    private BigDecimal discount_rate;
    @Pattern(regexp = "^[^\\s].*\\S.*$", message = "빈 문자열이나 공백만 입력할 수 없습니다")
    private Boolean is_soldout;
    @Pattern(regexp = "^[^\\s].*\\S.*$", message = "빈 문자열이나 공백만 입력할 수 없습니다")
    private String description;
    private List<AddImageDto> add_images = new ArrayList<>(); // 새로 추가할 이미지 리스트
    private List<ModifySeqImageDto> modify_images = new ArrayList<>(); // 위치 변경할 이미지 리스트
    private List<Long> remove_images = new ArrayList<>(); // 삭제할 이미지의 식별자 값 리스트
}
