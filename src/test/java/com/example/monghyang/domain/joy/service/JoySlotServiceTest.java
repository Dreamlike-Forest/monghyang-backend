package com.example.monghyang.domain.joy.service;

import com.example.monghyang.domain.brewery.entity.Brewery;
import com.example.monghyang.domain.brewery.entity.BreweryWeeklyBreakTime;
import com.example.monghyang.domain.brewery.entity.BreweryWeeklyOpenTime;
import com.example.monghyang.domain.brewery.repository.BreweryClosedDateRepository;
import com.example.monghyang.domain.brewery.repository.BreweryWeeklyBreakTimeRepository;
import com.example.monghyang.domain.brewery.repository.BreweryWeeklyOpenTimeRepository;
import com.example.monghyang.domain.global.ClosedStatus;
import com.example.monghyang.domain.global.DayOfWeek;
import com.example.monghyang.domain.global.advice.ApplicationError;
import com.example.monghyang.domain.global.advice.ApplicationException;
import com.example.monghyang.domain.joy.dto.slot.ReqFindJoySlotDateDto;
import com.example.monghyang.domain.joy.dto.slot.ResJoySlotDateDto;
import com.example.monghyang.domain.joy.dto.slot.ResJoySlotTimeDto;
import com.example.monghyang.domain.joy.entity.Joy;
import com.example.monghyang.domain.joy.entity.JoySlot;
import com.example.monghyang.domain.joy.entity.JoyWeeklyStartTime;
import com.example.monghyang.domain.joy.repository.JoyClosedDateRepository;
import com.example.monghyang.domain.joy.repository.JoyClosedStartTimeRepository;
import com.example.monghyang.domain.joy.repository.JoyRepository;
import com.example.monghyang.domain.joy.repository.JoySlotRepository;
import com.example.monghyang.domain.joy.repository.JoyWeeklyStartTimeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class JoySlotServiceTest {
    @Mock
    JoySlotRepository joySlotRepository;
    @Mock
    JoyRepository joyRepository;
    @Mock
    JoyWeeklyStartTimeRepository joyWeeklyStartTimeRepository;
    @Mock
    BreweryWeeklyOpenTimeRepository breweryWeeklyOpenTimeRepository;
    @Mock
    BreweryWeeklyBreakTimeRepository breweryWeeklyBreakTimeRepository;
    @Mock
    BreweryClosedDateRepository breweryClosedDateRepository;
    @Mock
    JoyClosedDateRepository joyClosedDateRepository;
    @Mock
    JoyClosedStartTimeRepository joyClosedStartTimeRepository;
    @InjectMocks
    JoySlotService joySlotService;

    @Test
    @DisplayName("비활성 체험의 예약 불가 날짜 조회는 JOY_NOT_FOUND로 거부한다")
    void get_impossible_date_rejects_inactive_joy() {
        Long joyId = 10L;
        ReqFindJoySlotDateDto dto = new ReqFindJoySlotDateDto();
        dto.setJoyId(joyId);
        dto.setYear(2026);
        dto.setMonth(6);
        given(joyRepository.findActiveById(joyId)).willReturn(Optional.empty());

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> joySlotService.getImpossibleDate(dto)
        );

        assertEquals(ApplicationError.JOY_NOT_FOUND, exception.getApplicationError());
        verify(breweryWeeklyOpenTimeRepository, never()).findActiveAndFutureOpenTimesInMonth(any(), any(), any());
    }

    @Test
    @DisplayName("삭제된 체험의 남은 자리 조회는 JOY_NOT_FOUND로 거부한다")
    void get_remaining_count_list_rejects_deleted_joy() {
        Long joyId = 10L;
        LocalDate targetDate = LocalDate.of(2026, 6, 1);
        given(joyRepository.findActiveById(joyId)).willReturn(Optional.empty());

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> joySlotService.getRemainingCountList(joyId, targetDate)
        );

        assertEquals(ApplicationError.JOY_NOT_FOUND, exception.getApplicationError());
        verify(joyWeeklyStartTimeRepository, never()).findActiveAndFutureStartTimesInMonth(any(), any(), any());
    }

    @Test
    @DisplayName("예약 가능 날짜 조회는 휴게시간과 겹치는 시작 시간을 유효 슬롯에서 제외한다")
    void get_impossible_date_excludes_break_time_slots() {
        Long joyId = 10L;
        Long breweryId = 20L;
        LocalDate startDate = LocalDate.of(2026, 6, 1);
        LocalDate endDate = LocalDate.of(2026, 7, 1);
        ReqFindJoySlotDateDto dto = new ReqFindJoySlotDateDto();
        dto.setJoyId(joyId);
        dto.setYear(2026);
        dto.setMonth(6);
        Joy joy = joy(breweryId, 60);

        given(joyRepository.findActiveById(joyId)).willReturn(Optional.of(joy));
        given(breweryWeeklyOpenTimeRepository.findActiveAndFutureOpenTimesInMonth(breweryId, startDate, endDate))
                .willReturn(List.of(openTime(LocalTime.of(9, 0), LocalTime.of(18, 0))));
        given(joyWeeklyStartTimeRepository.findActiveAndFutureStartTimesInMonth(joyId, startDate, endDate))
                .willReturn(List.of(startTime(LocalTime.of(12, 0))));
        given(breweryWeeklyBreakTimeRepository.findActiveAndFutureBreakTimesInMonth(breweryId, startDate, endDate))
                .willReturn(List.of(breakTime(LocalTime.of(12, 0), LocalTime.of(13, 0))));
        given(breweryClosedDateRepository.findConfirmedByBreweryIdAndMonth(breweryId, startDate, endDate, ClosedStatus.CONFIRMED))
                .willReturn(List.of());
        given(joyClosedDateRepository.findConfirmedByJoyIdAndMonth(joyId, startDate, endDate, ClosedStatus.CONFIRMED))
                .willReturn(List.of());
        given(joyClosedStartTimeRepository.findConfirmedByJoyIdAndMonth(joyId, startDate, endDate, ClosedStatus.CONFIRMED))
                .willReturn(List.of());
        given(joySlotRepository.findUnavailableJoySlotTimesByJoyIdAndMonth(joyId, startDate, endDate))
                .willReturn(List.of());

        ResJoySlotDateDto result = joySlotService.getImpossibleDate(dto);

        assertTrue(result.getJoy_unavailable_reservation_date().contains(LocalDate.of(2026, 6, 1)));
    }

    @Test
    @DisplayName("남은 자리 조회는 휴게시간과 겹치는 시작 시간을 응답에서 제외한다")
    void get_remaining_count_list_excludes_break_time_slots() {
        Long joyId = 10L;
        Long breweryId = 20L;
        LocalDate targetDate = LocalDate.of(2026, 6, 1);
        Joy joy = joy(breweryId, 60);
        given(joy.getMaxCount()).willReturn(10);

        given(joyRepository.findActiveById(joyId)).willReturn(Optional.of(joy));
        given(breweryWeeklyOpenTimeRepository.findActiveAndFutureOpenTimesInMonth(breweryId, targetDate, targetDate.plusDays(1)))
                .willReturn(List.of(openTime(LocalTime.of(9, 0), LocalTime.of(18, 0))));
        given(joyWeeklyStartTimeRepository.findActiveAndFutureStartTimesInMonth(joyId, targetDate, targetDate.plusDays(1)))
                .willReturn(List.of(startTime(LocalTime.of(10, 0)), startTime(LocalTime.of(12, 0))));
        given(breweryWeeklyBreakTimeRepository.findActiveAndFutureBreakTimesInMonth(breweryId, targetDate, targetDate.plusDays(1)))
                .willReturn(List.of(breakTime(LocalTime.of(12, 0), LocalTime.of(13, 0))));
        given(joySlotRepository.findByJoyIdAndDate(joyId, targetDate))
                .willReturn(List.of(
                        JoySlot.joyReservationOf(joy, targetDate, LocalTime.of(10, 0), 2),
                        JoySlot.joyReservationOf(joy, targetDate, LocalTime.of(12, 0), 2)
                ));

        ResJoySlotTimeDto result = joySlotService.getRemainingCountList(joyId, targetDate);

        assertEquals(List.of(LocalTime.of(10, 0)), result.getTime_info());
        assertEquals(1, result.getRemaining_count_list().size());
        assertEquals(LocalTime.of(10, 0), result.getRemaining_count_list().getFirst().getJoy_slot_reservation_time());
    }

    private Joy joy(Long breweryId, Integer timeUnit) {
        Brewery brewery = mock(Brewery.class);
        Joy joy = mock(Joy.class);
        given(brewery.getId()).willReturn(breweryId);
        given(joy.getBrewery()).willReturn(brewery);
        given(joy.getTimeUnit()).willReturn(timeUnit);
        return joy;
    }

    private BreweryWeeklyOpenTime openTime(LocalTime openTime, LocalTime closeTime) {
        return BreweryWeeklyOpenTime.builder()
                .brewery(mock(Brewery.class))
                .dayOfWeek(DayOfWeek.Mon)
                .openTime(openTime)
                .closeTime(closeTime)
                .effectiveDate(LocalDate.of(2026, 6, 1))
                .build();
    }

    private JoyWeeklyStartTime startTime(LocalTime startTime) {
        return JoyWeeklyStartTime.joyDayOfWeekStartTimeEffectiveDateOf(
                mock(Joy.class),
                DayOfWeek.Mon,
                startTime,
                LocalDate.of(2026, 6, 1)
        );
    }

    private BreweryWeeklyBreakTime breakTime(LocalTime breakStart, LocalTime breakEnd) {
        return BreweryWeeklyBreakTime.builder()
                .brewery(mock(Brewery.class))
                .dayOfWeek(DayOfWeek.Mon)
                .breakStart(breakStart)
                .breakEnd(breakEnd)
                .effectiveDate(LocalDate.of(2026, 6, 1))
                .build();
    }
}
