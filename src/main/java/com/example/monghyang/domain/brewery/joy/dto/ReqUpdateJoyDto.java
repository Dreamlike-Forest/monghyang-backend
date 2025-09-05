package com.example.monghyang.domain.brewery.joy.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ReqUpdateJoyDto extends ReqJoyDto {
    @NotNull(message = "수정하려는 체험의 식별자 정보를 입력해주세요.")
    private Long id;
    private Integer discount_rate;
    private Boolean is_soldout;
}
