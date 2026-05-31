package com.example.monghyang.domain.auth.dto;

import com.example.monghyang.domain.global.DayOfWeek;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
public class BreweryScheduleDto {
    /** 운영 요일입니다. */
    @NotNull(message = "요일 정보를 입력해주세요.")
    private DayOfWeek day_of_week;

    /** 해당 요일의 운영 시작 시간입니다. */
    @NotNull(message = "해당 요일의 운영 시작 시간을 입력해주세요.")
    private LocalTime open_time;

    /** 해당 요일의 운영 종료 시간입니다. */
    @NotNull(message = "해당 요일의 운영 종료 시간을 입력해주세요.")
    private LocalTime close_time;

    /** 해당 요일의 휴게 시작 시간입니다. 휴게시간이 없으면 break_end와 함께 비웁니다. */
    private LocalTime break_start;

    /** 해당 요일의 휴게 종료 시간입니다. 휴게시간이 없으면 break_start와 함께 비웁니다. */
    private LocalTime break_end;
}
