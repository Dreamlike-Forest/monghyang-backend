package com.example.monghyang.domain.joy.repository;

import com.example.monghyang.domain.global.DayOfWeek;
import com.example.monghyang.domain.joy.entity.JoyWeeklyStartTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class JoyWeeklyStartTimeRepositoryTest {

    @Test
    @DisplayName("예약일에 활성화된 체험 시작 시간 스냅샷을 조회한다")
    void find_active_start_times_by_joy_id_and_date() {
        JoyWeeklyStartTimeRepository repository = mock(JoyWeeklyStartTimeRepository.class);
        Long joyId = 1L;
        LocalDate targetDate = LocalDate.of(2026, 6, 1);

        List<JoyWeeklyStartTime> ignored = repository.findActiveStartTimesByJoyIdAndDate(
                joyId,
                targetDate,
                DayOfWeek.Mon
        );

        verify(repository).findActiveStartTimesByJoyIdAndDate(joyId, targetDate, DayOfWeek.Mon);
    }

    @Test
    @DisplayName("같은 적용일의 기존 체험 시작 시간 스냅샷을 삭제한다")
    void delete_by_joy_id_and_effective_date() {
        JoyWeeklyStartTimeRepository repository = mock(JoyWeeklyStartTimeRepository.class);
        Long joyId = 1L;
        LocalDate effectiveDate = LocalDate.of(2026, 6, 1);

        repository.deleteByJoyIdAndEffectiveDate(joyId, effectiveDate);

        verify(repository).deleteByJoyIdAndEffectiveDate(joyId, effectiveDate);
    }
}
