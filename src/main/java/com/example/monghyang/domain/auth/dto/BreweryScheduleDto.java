package com.example.monghyang.domain.auth.dto;

import com.example.monghyang.domain.global.DayOfWeek;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
public class BreweryScheduleDto {
    @NotNull(message = "요일 정보를 입력해주세요.")
    private DayOfWeek day_of_week;
    @NotNull(message = "해당 요일의 운영 시작 시간을 입력해주세요.")
    private LocalTime open_time;
    @NotNull(message = "해당 요일의 운영 종료 시간을 입력해주세요.")
    private LocalTime close_time;
    // 휴게시간은 선택사항입니다. 단, 시작/종료 시간이 둘 다 없거나 둘 다 있어야 합니다.
    private LocalTime break_start;
    private LocalTime break_end;
}
