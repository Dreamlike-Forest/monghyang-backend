package com.example.monghyang.domain.brewery.joy.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class ReqUpdateJoyDto {
    @NotNull(message = "수정하려는 체험의 식별자 정보를 입력해주세요.")
    private Long id;
    private Integer discount_rate;
    private String name;
    private String place;
    private String detail;
    private Integer origin_price;
    private MultipartFile image; // 새 체험 이미지
    private Boolean is_soldout;
}
