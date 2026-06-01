package com.example.monghyang.domain.image.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@Schema(description = "새 이미지 추가 요청 단위")
public class AddImageDto { // 이미지 새로 추가할 때 하나의 이미지 파일과 순서 정보를 묶어서 받기 위한 dto
    @Schema(description = "새로 추가할 이미지 파일입니다.", type = "string", format = "binary", requiredMode = Schema.RequiredMode.REQUIRED)
    private MultipartFile image; // 새로 추가할 이미지 파일
    @Schema(description = "이미지 노출 순서입니다. 1부터 5까지 허용되며, seq=1이 대표 이미지입니다.", example = "1", minimum = "1", maximum = "5", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer seq;
}
