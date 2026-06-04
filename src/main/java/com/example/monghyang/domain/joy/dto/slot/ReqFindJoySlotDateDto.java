package com.example.monghyang.domain.joy.dto.slot;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "체험 예약 불가능 날짜 조회 요청 정보")
public class ReqFindJoySlotDateDto {
    @Schema(description = "조회할 체험 식별자입니다.", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "체험 식별자를 입력해주세요.")
    private Long joyId;
    @Schema(description = "조회할 연도입니다.", example = "2026", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "년도 정보를 입력해주세요.")
    private Integer year;
    @Schema(description = "조회할 월입니다. 1보다 작으면 1, 12보다 크면 12로 보정됩니다.", example = "6", minimum = "1", maximum = "12", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "월 정보를 입력해주세요.")
    private Integer month;
}
