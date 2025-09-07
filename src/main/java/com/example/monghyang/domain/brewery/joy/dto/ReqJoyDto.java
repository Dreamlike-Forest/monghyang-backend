package com.example.monghyang.domain.brewery.joy.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class ReqJoyDto {
    @NotNull(message = "체험의 이름 정보를 입력해주세요.")
    private String name;
    @NotNull(message = "체험의 장소 정보를 입력해주세요.")
    private String place;
    @NotNull(message = "체험의 설명 등 상세 정보를 입력해주세요.")
    private String detail;
    @NotNull(message = "체험의 1인 당 정가 정보를 정수로 입력해주세요.")
    private Integer origin_price;
    private MultipartFile image;
}
