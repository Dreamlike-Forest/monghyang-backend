package com.example.monghyang.domain.image.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "기존 이미지 순서 변경 요청 단위")
public class ModifySeqImageDto {
    @Schema(description = "순서를 변경할 기존 이미지 식별자입니다.", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long image_id;
    @Schema(description = "변경할 이미지 노출 순서입니다. 1부터 5까지 허용되며, seq=1이 대표 이미지입니다.", example = "2", minimum = "1", maximum = "5", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer seq;
}
