package com.example.monghyang.domain.joy.dto;

import com.example.monghyang.domain.global.DayOfWeek;
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
public class JoyScheduleDto {
    /** 체험 시작 시간이 적용되는 요일입니다. */
    @NotNull(message = "체험 시작 시간의 요일을 입력해주세요.")
    private DayOfWeek day_of_week;

    /** 해당 요일에 예약 가능한 체험 시작 시간 목록입니다. */
    @NotEmpty(message = "체험 시작 시간 목록을 입력해주세요.")
    private List<@NotNull(message = "체험 시작 시간은 null일 수 없습니다.") LocalTime> start_times;
}
