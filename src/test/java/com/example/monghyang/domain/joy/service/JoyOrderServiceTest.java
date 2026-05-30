package com.example.monghyang.domain.joy.service;

import com.example.monghyang.domain.batch.dto.JoyStatusHistoryBatchRow;
import com.example.monghyang.domain.batch.service.JoyOrderBatchService;
import com.example.monghyang.domain.batch.service.JoyOrderRefundService;
import com.example.monghyang.domain.brewery.dto.JoyInfoDto;
import com.example.monghyang.domain.brewery.entity.Brewery;
import com.example.monghyang.domain.brewery.entity.BreweryWeeklyBreakTime;
import com.example.monghyang.domain.brewery.repository.BreweryRepository;
import com.example.monghyang.domain.brewery.repository.BreweryWeeklyBreakTimeRepository;
import com.example.monghyang.domain.global.DayOfWeek;
import com.example.monghyang.domain.global.advice.ApplicationError;
import com.example.monghyang.domain.global.advice.ApplicationException;
import com.example.monghyang.domain.joy.dto.ReqUpdateJoyOrderDto;
import com.example.monghyang.domain.joy.entity.Joy;
import com.example.monghyang.domain.joy.entity.JoyOrder;
import com.example.monghyang.domain.joy.entity.JoyPaymentStatus;
import com.example.monghyang.domain.joy.entity.JoyWeeklyStartTime;
import com.example.monghyang.domain.joy.repository.JoyOrderRepository;
import com.example.monghyang.domain.joy.repository.JoyRepository;
import com.example.monghyang.domain.joy.repository.JoyStatusHistoryRepository;
import com.example.monghyang.domain.joy.repository.JoyWeeklyStartTimeRepository;
import com.example.monghyang.domain.users.entity.Users;
import com.example.monghyang.domain.users.repository.UsersRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class JoyOrderServiceTest {
    @Mock
    JoyOrderRepository joyOrderRepository;
    @Mock
    UsersRepository usersRepository;
    @Mock
    JoyRepository joyRepository;
    @Mock
    BreweryRepository breweryRepository;
    @Mock
    JoyStatusHistoryRepository joyStatusHistoryRepository;
    @Mock
    JoySlotService joySlotService;
    @Mock
    JoyOrderBatchService joyOrderBatchService;
    @Mock
    JoyOrderRefundService joyOrderRefundService;
    @Mock
    JoyWeeklyStartTimeRepository joyWeeklyStartTimeRepository;
    @Mock
    BreweryWeeklyBreakTimeRepository breweryWeeklyBreakTimeRepository;
    @InjectMocks
    JoyOrderService joyOrderService;

    @Test
    @DisplayName("예약 슬롯 증가 시 활성 체험 시작 시간 스냅샷에 없는 시간은 거부한다")
    void reservation_joy_slot_count_rejects_time_not_in_active_snapshot() {
        Long joyId = 10L;
        LocalDate reservationDate = LocalDate.of(2026, 6, 1);
        LocalTime reservationTime = LocalTime.of(11, 0);
        JoyWeeklyStartTime activeStartTime = startTime(LocalTime.of(10, 0));
        given(breweryRepository.findJoyTimeInfoByJoyId(joyId, reservationDate, DayOfWeek.Mon))
                .willReturn(Optional.of(new JoyInfoDto(20L, LocalTime.of(9, 0), LocalTime.of(18, 0), 60, 10, 1)));
        given(breweryWeeklyBreakTimeRepository.findActiveBreakTimesByBreweryIdAndDate(20L, reservationDate, DayOfWeek.Mon))
                .willReturn(List.of());
        given(joyWeeklyStartTimeRepository.findActiveStartTimesByJoyIdAndDate(joyId, reservationDate, DayOfWeek.Mon))
                .willReturn(List.of(activeStartTime));

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> joyOrderService.reservationJoySlotCount(joyId, reservationDate, reservationTime, 2)
        );

        assertEquals(ApplicationError.JOY_ORDER_TIME_INVALID, exception.getApplicationError());
        verify(joySlotService, never()).reservationJoySlot(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("사용자 예약 변경 시 활성 체험 시작 시간 스냅샷에 없는 시간은 거부한다")
    void update_reservation_rejects_time_not_in_active_snapshot() {
        Long userId = 1L;
        Long joyId = 10L;
        LocalDate reservationDate = LocalDate.of(2026, 6, 1);
        ReqUpdateJoyOrderDto dto = updateDto(99L, reservationDate, LocalTime.of(11, 0), 2);
        JoyOrder joyOrder = joyOrder(joyId);
        Users users = mock(Users.class);
        JoyWeeklyStartTime activeStartTime = startTime(LocalTime.of(10, 0));
        given(users.getId()).willReturn(userId);
        given(joyOrder.getUsers()).willReturn(users);
        given(joyOrder.getReservation()).willReturn(LocalDate.of(2026, 6, 2).atTime(LocalTime.of(10, 0)));
        given(joyOrderRepository.findById(dto.getId())).willReturn(Optional.of(joyOrder));
        given(breweryRepository.findJoyTimeInfoByJoyId(joyId, reservationDate, DayOfWeek.Mon))
                .willReturn(Optional.of(new JoyInfoDto(20L, LocalTime.of(9, 0), LocalTime.of(18, 0), 60, 10, 1)));
        given(breweryWeeklyBreakTimeRepository.findActiveBreakTimesByBreweryIdAndDate(20L, reservationDate, DayOfWeek.Mon))
                .willReturn(List.of());
        given(joyWeeklyStartTimeRepository.findActiveStartTimesByJoyIdAndDate(joyId, reservationDate, DayOfWeek.Mon))
                .willReturn(List.of(activeStartTime));

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> joyOrderService.updateReservation(userId, dto)
        );

        assertEquals(ApplicationError.JOY_ORDER_TIME_INVALID, exception.getApplicationError());
        verify(joySlotService, never()).reservationJoySlot(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("양조장 예약 변경은 체험 ID로 새 슬롯을 증가시키고 기존 예약 기준으로 슬롯을 감소시킨다")
    void update_reservation_by_brewery_uses_joy_id_and_previous_reservation_for_slots() {
        Long userId = 1L;
        Long joyOrderId = 99L;
        Long joyId = 10L;
        LocalDate oldDate = LocalDate.of(2026, 6, 2);
        LocalTime oldTime = LocalTime.of(10, 0);
        LocalDate newDate = LocalDate.of(2026, 6, 3);
        LocalTime newTime = LocalTime.of(11, 0);
        JoyOrder joyOrder = joyOrder(joyId);
        ReqUpdateJoyOrderDto dto = updateDto(joyOrderId, newDate, newTime, 3);
        JoyWeeklyStartTime activeStartTime = startTime(newTime);
        given(joyOrder.getReservation()).willReturn(oldDate.atTime(oldTime));
        given(joyOrder.getCount()).willReturn(2);
        given(joyOrderRepository.findByIdAndBreweryUserId(joyOrderId, userId)).willReturn(Optional.of(joyOrder));
        given(breweryRepository.findJoyTimeInfoByJoyId(joyId, newDate, DayOfWeek.Wed))
                .willReturn(Optional.of(new JoyInfoDto(20L, LocalTime.of(9, 0), LocalTime.of(18, 0), 60, 10, 1)));
        given(breweryWeeklyBreakTimeRepository.findActiveBreakTimesByBreweryIdAndDate(20L, newDate, DayOfWeek.Wed))
                .willReturn(List.of());
        given(joyWeeklyStartTimeRepository.findActiveStartTimesByJoyIdAndDate(joyId, newDate, DayOfWeek.Wed))
                .willReturn(List.of(activeStartTime));

        joyOrderService.updateReservationByBrewery(userId, dto);

        verify(joySlotService).reservationJoySlot(joyId, newDate, newTime, 3, 10);
        verify(joySlotService).decrementJoySlotCount(joyId, oldDate, oldTime, 2);
    }

    @Test
    @DisplayName("예약 슬롯 증가는 양조장 휴게시간과 겹치는 시간대를 거부한다")
    void reservation_joy_slot_count_rejects_break_time_overlap() {
        Long joyId = 10L;
        Long breweryId = 20L;
        LocalDate reservationDate = LocalDate.of(2026, 6, 1);
        LocalTime reservationTime = LocalTime.of(12, 0);

        given(breweryRepository.findJoyTimeInfoByJoyId(joyId, reservationDate, DayOfWeek.Mon))
                .willReturn(Optional.of(new JoyInfoDto(breweryId, LocalTime.of(9, 0), LocalTime.of(18, 0), 60, 10, 1)));
        given(breweryWeeklyBreakTimeRepository.findActiveBreakTimesByBreweryIdAndDate(breweryId, reservationDate, DayOfWeek.Mon))
                .willReturn(List.of(breakTime(LocalTime.of(12, 0), LocalTime.of(13, 0))));

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> joyOrderService.reservationJoySlotCount(joyId, reservationDate, reservationTime, 2)
        );

        assertEquals(ApplicationError.JOY_ORDER_TIME_INVALID, exception.getApplicationError());
        verify(joySlotService, never()).reservationJoySlot(any(), any(), any(), any(), any());
    }

    @Test
    @SuppressWarnings("unchecked")
    @DisplayName("체험 일정 변경 환불 처리는 적용일 이후 PAID 예약을 환불 요청 상태로 전환하고 이력을 저장한다")
    void set_refund_requested_by_joy_schedule_change_updates_paid_orders_and_inserts_histories() {
        Long joyId = 10L;
        LocalDate effectiveDate = LocalDate.of(2026, 6, 1);
        given(joyOrderRepository.findIdByJoyIdAndReservationFromAndPaymentStatusAndIsDeleted(
                joyId,
                effectiveDate.atStartOfDay(),
                JoyPaymentStatus.PAID,
                false
        )).willReturn(List.of(1L, 2L));

        joyOrderService.setRefundRequestedByJoyScheduleChange(joyId, effectiveDate);

        verify(joyOrderRepository).updatePaymentStatusByJoyIdListAndStatus(
                List.of(1L, 2L),
                JoyPaymentStatus.REFUND_REQUESTED
        );
        ArgumentCaptor<List<JoyStatusHistoryBatchRow>> captor = ArgumentCaptor.forClass(List.class);
        verify(joyOrderBatchService).batchInsert(captor.capture());
        assertEquals(2, captor.getValue().size());
        assertEquals("체험 일정 변경", captor.getValue().getFirst().getReasonCode());
    }

    private ReqUpdateJoyOrderDto updateDto(Long joyOrderId, LocalDate date, LocalTime time, Integer count) {
        ReqUpdateJoyOrderDto dto = new ReqUpdateJoyOrderDto();
        dto.setId(joyOrderId);
        dto.setReservation_date(date);
        dto.setReservation_time(time);
        dto.setCount(count);
        return dto;
    }

    private JoyWeeklyStartTime startTime(LocalTime startTime) {
        return JoyWeeklyStartTime.joyDayOfWeekStartTimeEffectiveDateOf(
                mock(Joy.class),
                DayOfWeek.Mon,
                startTime,
                LocalDate.of(2026, 6, 1)
        );
    }

    private JoyOrder joyOrder(Long joyId) {
        Joy joy = mock(Joy.class);
        JoyOrder joyOrder = mock(JoyOrder.class);
        given(joy.getId()).willReturn(joyId);
        given(joyOrder.getJoy()).willReturn(joy);
        return joyOrder;
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
