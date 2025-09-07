package com.example.monghyang.domain.product.dto;

import com.example.monghyang.domain.image.dto.AddImageDto;
import com.example.monghyang.domain.image.dto.ModifySeqImageDto;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class UpdateProductDto {
    @NotNull(message = "수정하려는 상품의 식별자 정보를 입력해주세요.")
    private Long id;
    private String name;
    private Double alcohol;
    private Boolean is_online_sell;
    private Integer volume;
    private Integer origin_price;
    private Integer discount_rate;
    private Boolean is_soldout;
    private String description;
    private List<AddImageDto> add_images = new ArrayList<>(); // 새로 추가할 이미지 리스트
    private List<ModifySeqImageDto> modify_images = new ArrayList<>(); // 위치 변경할 이미지 리스트
    private List<Long> remove_images = new ArrayList<>(); // 삭제할 이미지의 식별자 값 리스트
}
