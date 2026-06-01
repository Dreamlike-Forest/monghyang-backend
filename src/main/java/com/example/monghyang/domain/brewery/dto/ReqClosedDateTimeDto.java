package com.example.monghyang.domain.brewery.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@Schema(description = "양조장 별도 휴무일 요청 정보")
public class ReqClosedDateTimeDto {
    @Schema(description = "별도 휴무일 날짜입니다. yyyy-MM-dd 형식이며 오늘 또는 미래 날짜만 처리할 수 있습니다.", example = "2026-06-15", type = "string", format = "date", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "별도 휴무일 날짜 정보를 입력해주세요.")
    private LocalDate closed_date;
    @Schema(description = "별도 휴무 시간입니다. 현재 양조장 휴무일 지정/확정/해제 구현에서는 실제 처리 조건으로 사용하지 않습니다.", example = "10:00:00", type = "string", format = "time", nullable = true)
    private LocalTime closed_time; // not required
    @Schema(description = "별도 휴무 사유입니다. 지정 시도 API에서 저장되며 필수는 아닙니다.", example = "양조장 내부 행사", nullable = true)
    private String reason;
}
