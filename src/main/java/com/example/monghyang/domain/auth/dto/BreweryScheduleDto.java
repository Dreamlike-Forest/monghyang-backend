package com.example.monghyang.domain.auth.dto;

import com.example.monghyang.domain.global.DayOfWeek;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
@Schema(description = "양조장 특정 요일의 운영시간 및 휴게시간 정보")
public class BreweryScheduleDto {
    /** 운영 요일입니다. */
    @Schema(description = "운영 요일입니다.", example = "Mon", allowableValues = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"}, requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "요일 정보를 입력해주세요.")
    private DayOfWeek day_of_week;

    /** 해당 요일의 운영 시작 시간입니다. */
    @Schema(description = "해당 요일의 운영 시작 시간입니다. HH:mm:ss 형식으로 전달합니다.", example = "09:00:00", type = "string", format = "time", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "해당 요일의 운영 시작 시간을 입력해주세요.")
    private LocalTime open_time;

    /** 해당 요일의 운영 종료 시간입니다. */
    @Schema(description = "해당 요일의 운영 종료 시간입니다. open_time보다 늦어야 합니다.", example = "18:00:00", type = "string", format = "time", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "해당 요일의 운영 종료 시간을 입력해주세요.")
    private LocalTime close_time;

    /** 해당 요일의 휴게 시작 시간입니다. 휴게시간이 없으면 break_end와 함께 비웁니다. */
    @Schema(description = "해당 요일의 휴게 시작 시간입니다. 휴게시간이 없으면 break_end와 함께 비웁니다.", example = "12:00:00", type = "string", format = "time", nullable = true)
    private LocalTime break_start;

    /** 해당 요일의 휴게 종료 시간입니다. 휴게시간이 없으면 break_start와 함께 비웁니다. */
    @Schema(description = "해당 요일의 휴게 종료 시간입니다. 휴게시간이 없으면 break_start와 함께 비웁니다.", example = "13:00:00", type = "string", format = "time", nullable = true)
    private LocalTime break_end;
}
