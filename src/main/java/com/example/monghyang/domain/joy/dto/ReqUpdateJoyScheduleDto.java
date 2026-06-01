package com.example.monghyang.domain.joy.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

/**
 * 체험 요일별 시작 시간 스냅샷을 새 적용일 기준으로 변경하는 요청입니다.
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "체험 요일별 시작 시간 일정 변경 요청 정보")
public class ReqUpdateJoyScheduleDto {
    /** 일정을 변경할 체험 식별자입니다. */
    @Schema(description = "일정을 변경할 체험 식별자입니다.", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "체험 식별자를 입력해주세요.")
    private Long joyId;

    /** 새 체험 일정 스냅샷이 적용되기 시작하는 날짜입니다. */
    @Schema(description = "새 체험 일정 스냅샷이 적용되기 시작하는 날짜입니다. yyyy-MM-dd 형식이며 오늘 또는 미래 날짜만 허용됩니다.", example = "2026-06-15", type = "string", format = "date", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "체험 일정 적용일을 입력해주세요.")
    @FutureOrPresent(message = "체험 일정 적용일은 오늘 또는 미래 날짜여야 합니다.")
    private LocalDate effective_date;

    /** 새 적용일부터 저장할 전체 요일별 체험 시작 시간 목록입니다. 목록에 없는 요일은 체험 미운영으로 해석합니다. */
    @Schema(description = "새 적용일부터 저장할 전체 요일별 체험 시작 시간 목록입니다. 목록에 없는 요일은 체험 미운영으로 해석합니다.", requiredMode = Schema.RequiredMode.REQUIRED)
    @Valid
    @NotEmpty(message = "체험 일정 목록을 입력해주세요.")
    private List<JoyScheduleDto> schedules;
}
