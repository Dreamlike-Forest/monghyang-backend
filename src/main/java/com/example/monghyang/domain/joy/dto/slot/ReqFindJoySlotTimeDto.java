package com.example.monghyang.domain.joy.dto.slot;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Schema(description = "체험 특정 날짜 예약 가능 시간 조회 요청 정보")
public class ReqFindJoySlotTimeDto {
    @Schema(description = "조회할 체험 식별자입니다.", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "체험 식별자를 입력해주세요.")
    private Long joyId;
    @Schema(description = "예약 가능 시간을 조회할 날짜입니다. yyyy-MM-dd 형식입니다.", example = "2026-06-15", type = "string", format = "date", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "시간대를 조회하려는 날짜 정보를 입력해주세요.")
    private LocalDate date;
}
