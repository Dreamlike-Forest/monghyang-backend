package com.example.monghyang.domain.brewery.repository;

import com.example.monghyang.domain.global.DayOfWeek;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class BreweryWeeklyBreakTimeRepositoryTest {

    @Test
    @DisplayName("예약일에 활성화된 양조장 휴게시간 스냅샷을 조회한다")
    void find_active_break_times_by_brewery_id_and_date() {
        BreweryWeeklyBreakTimeRepository repository = mock(BreweryWeeklyBreakTimeRepository.class);
        Long breweryId = 1L;
        LocalDate targetDate = LocalDate.of(2026, 6, 1);

        repository.findActiveBreakTimesByBreweryIdAndDate(breweryId, targetDate, DayOfWeek.Mon);

        verify(repository).findActiveBreakTimesByBreweryIdAndDate(breweryId, targetDate, DayOfWeek.Mon);
    }

    @Test
    @DisplayName("월 범위에서 유효한 양조장 휴게시간 스냅샷을 조회한다")
    void find_active_and_future_break_times_in_month() {
        BreweryWeeklyBreakTimeRepository repository = mock(BreweryWeeklyBreakTimeRepository.class);
        Long breweryId = 1L;
        LocalDate startDate = LocalDate.of(2026, 6, 1);
        LocalDate endDate = LocalDate.of(2026, 7, 1);

        repository.findActiveAndFutureBreakTimesInMonth(breweryId, startDate, endDate);

        verify(repository).findActiveAndFutureBreakTimesInMonth(breweryId, startDate, endDate);
    }
}
