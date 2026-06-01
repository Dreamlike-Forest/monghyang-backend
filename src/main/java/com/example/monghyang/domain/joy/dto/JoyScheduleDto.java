package com.example.monghyang.domain.joy.dto;

import com.example.monghyang.domain.global.DayOfWeek;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;
import java.util.List;

/**
 * 체험의 특정 요일에 예약 가능한 시작 시간 목록을 전달하는 요청 단위입니다.
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "체험 특정 요일의 예약 가능 시작 시간 목록")
public class JoyScheduleDto {
    /** 체험 시작 시간이 적용되는 요일입니다. */
    @Schema(description = "체험 시작 시간이 적용되는 요일입니다.", example = "Sat", allowableValues = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"}, requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "체험 시작 시간의 요일을 입력해주세요.")
    private DayOfWeek day_of_week;

    /** 해당 운영 요일에 예약 가능한 체험 시작 시간 목록입니다. 미운영 요일은 이 DTO를 보내지 않습니다. */
    @Schema(description = "해당 요일에 예약 가능한 체험 시작 시간 목록입니다. HH:mm:ss 형식으로 전달하며, 중복 시간은 허용되지 않습니다.", example = "[\"10:00:00\", \"14:00:00\"]", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "체험 시작 시간 목록을 입력해주세요.")
    private List<@NotNull(message = "체험 시작 시간은 null일 수 없습니다.") LocalTime> start_times;
}
