package com.example.monghyang.domain.brewery.dto;

import com.example.monghyang.domain.auth.dto.BreweryScheduleDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

/**
 * 양조장 운영시간/휴게시간 스케줄 변경 요청 DTO.
 * 주간 스냅샷 단위로 요일별 운영시간 및 휴게시간을 일괄 교체합니다.
 */
@Getter
@Setter
@Schema(description = "양조장 운영시간/휴게시간 일정 변경 요청 정보")
public class ReqUpdateBreweryScheduleDto {

    /**
     * 스케줄 적용 시작일.
     * 오늘 이후(당일 포함) 날짜만 허용합니다.
     */
    @Schema(description = "새 운영/휴게시간 스냅샷이 적용되기 시작하는 날짜입니다. yyyy-MM-dd 형식이며 오늘 또는 미래 날짜만 허용됩니다.", example = "2026-06-15", type = "string", format = "date", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "일정 적용 시작일(effective_date)을 입력해주세요.")
    @FutureOrPresent(message = "적용 시작일은 오늘 이후 날짜만 입력할 수 있습니다.")
    private LocalDate effective_date;

    /**
     * 새 적용일부터 사용할 전체 요일별 운영/휴게시간 목록.
     * 목록에 없는 요일은 미운영으로 해석하고, 포함된 요일에서 휴게 시작/종료가 둘 다 없으면 휴게시간 없음으로 해석합니다.
     */
    @Schema(description = "새 적용일부터 사용할 전체 요일별 운영/휴게시간 목록입니다. 목록에 없는 요일은 미운영으로 해석합니다.", requiredMode = Schema.RequiredMode.REQUIRED)
    @Valid
    @NotNull(message = "운영 시간대 정보 목록을 입력해주세요.")
    @Size(min = 1, message = "운영 시간대 정보는 최소 1개 이상 입력해야 합니다.")
    private List<BreweryScheduleDto> schedules;
}
