package com.example.monghyang.domain.joy.dto;

import com.example.monghyang.domain.global.DayOfWeek;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

class JoyScheduleDtoTest {
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    @DisplayName("체험 생성 요청은 요일별 시작 시간 목록이 없으면 검증에 실패한다")
    void req_joy_dto_requires_schedules() {
        ReqJoyDto dto = new ReqJoyDto();
        dto.setName("막걸리 빚기");
        dto.setPlace("체험장");
        dto.setDetail("체험 설명");
        dto.setTime_unit(60);
        dto.setOrigin_price(java.math.BigDecimal.valueOf(10000));
        dto.setMax_count(10);
        dto.setSchedules(Collections.emptyList());

        assertFalse(validator.validate(dto).isEmpty());
    }

    @Test
    @DisplayName("체험 일정 요청은 시작 시간 목록이 비어 있으면 검증에 실패한다")
    void joy_schedule_dto_requires_start_times() {
        JoyScheduleDto dto = new JoyScheduleDto();
        dto.setDay_of_week(DayOfWeek.Mon);
        dto.setStart_times(Collections.emptyList());

        assertFalse(validator.validate(dto).isEmpty());
    }

    @Test
    @DisplayName("체험 일정 변경 요청은 적용일과 요일별 시작 시간 목록이 없으면 검증에 실패한다")
    void req_update_joy_schedule_dto_requires_effective_date_and_schedules() {
        ReqUpdateJoyScheduleDto dto = new ReqUpdateJoyScheduleDto();
        dto.setJoyId(1L);
        dto.setEffective_date(LocalDate.now().plusDays(1));
        dto.setSchedules(List.of(schedule(DayOfWeek.Tue, Collections.emptyList())));

        assertFalse(validator.validate(dto).isEmpty());
    }

    private JoyScheduleDto schedule(DayOfWeek dayOfWeek, List<LocalTime> startTimes) {
        JoyScheduleDto dto = new JoyScheduleDto();
        dto.setDay_of_week(dayOfWeek);
        dto.setStart_times(startTimes);
        return dto;
    }
}
