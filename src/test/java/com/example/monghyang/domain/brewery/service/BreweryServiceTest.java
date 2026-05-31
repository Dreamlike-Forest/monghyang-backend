package com.example.monghyang.domain.brewery.service;

import com.example.monghyang.domain.auth.dto.BreweryScheduleDto;
import com.example.monghyang.domain.batch.service.JoyOrderBatchService;
import com.example.monghyang.domain.brewery.dto.ReqUpdateBreweryScheduleDto;
import com.example.monghyang.domain.brewery.entity.Brewery;
import com.example.monghyang.domain.brewery.repository.BreweryClosedDateRepository;
import com.example.monghyang.domain.brewery.repository.BreweryImageRepository;
import com.example.monghyang.domain.brewery.repository.BreweryRepository;
import com.example.monghyang.domain.brewery.repository.BreweryWeeklyBreakTimeRepository;
import com.example.monghyang.domain.brewery.repository.BreweryWeeklyOpenTimeRepository;
import com.example.monghyang.domain.brewery.repository.RegionTypeRepository;
import com.example.monghyang.domain.brewery.tag.BreweryTagRepository;
import com.example.monghyang.domain.global.DayOfWeek;
import com.example.monghyang.domain.global.advice.ApplicationError;
import com.example.monghyang.domain.global.advice.ApplicationException;
import com.example.monghyang.domain.image.service.StorageService;
import com.example.monghyang.domain.joy.repository.JoyOrderRepository;
import com.example.monghyang.domain.joy.repository.JoyRepository;
import com.example.monghyang.domain.joy.repository.JoyStatusHistoryRepository;
import com.example.monghyang.domain.joy.service.JoyOrderService;
import com.example.monghyang.domain.product.service.ProductService;
import com.example.monghyang.domain.users.repository.UsersRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

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
class BreweryServiceTest {
    @Mock
    BreweryRepository breweryRepository;
    @Mock
    UsersRepository usersRepository;
    @Mock
    BCryptPasswordEncoder bCryptPasswordEncoder;
    @Mock
    BreweryImageRepository breweryImageRepository;
    @Mock
    StorageService storageService;
    @Mock
    BreweryTagRepository breweryTagRepository;
    @Mock
    JoyRepository joyRepository;
    @Mock
    ProductService productService;
    @Mock
    RegionTypeRepository regionTypeRepository;
    @Mock
    BreweryClosedDateRepository breweryClosedDateRepository;
    @Mock
    JoyOrderRepository joyOrderRepository;
    @Mock
    JoyStatusHistoryRepository joyStatusHistoryRepository;
    @Mock
    JoyOrderService joyOrderService;
    @Mock
    JoyOrderBatchService joyOrderBatchService;
    @Mock
    BreweryWeeklyOpenTimeRepository breweryWeeklyOpenTimeRepository;
    @Mock
    BreweryWeeklyBreakTimeRepository breweryWeeklyBreakTimeRepository;
    @InjectMocks
    BreweryService breweryService;

    @Test
    @DisplayName("탈퇴한 양조장 관리자도 운영 시간 스냅샷을 변경할 수 있다")
    void update_brewery_schedule_allows_deleted_brewery_owner() {
        Long userId = 1L;
        Long breweryId = 5L;
        ReqUpdateBreweryScheduleDto dto = updateScheduleDto();
        Brewery brewery = mock(Brewery.class);
        given(brewery.getId()).willReturn(breweryId);
        given(breweryRepository.findByUserId(userId)).willReturn(Optional.of(brewery));

        breweryService.updateBrewerySchedule(userId, dto);

        verify(breweryWeeklyOpenTimeRepository).deleteByBreweryIdAndEffectiveDate(breweryId, dto.getEffective_date());
        verify(breweryWeeklyOpenTimeRepository).save(any());
        verify(joyOrderService).setRefundRequestedByScheduleChange(breweryId, dto.getEffective_date());
    }

    @Test
    @DisplayName("양조장 일정 변경에서 휴게시간이 비어 있으면 새 버전에 휴게시간 row를 저장하지 않는다")
    void update_brewery_schedule_does_not_save_break_time_when_break_fields_are_empty() {
        Long userId = 1L;
        Long breweryId = 5L;
        ReqUpdateBreweryScheduleDto dto = updateScheduleDto();
        Brewery brewery = mock(Brewery.class);
        given(brewery.getId()).willReturn(breweryId);
        given(breweryRepository.findByUserId(userId)).willReturn(Optional.of(brewery));

        breweryService.updateBrewerySchedule(userId, dto);

        verify(breweryWeeklyBreakTimeRepository).deleteByBreweryIdAndEffectiveDate(breweryId, dto.getEffective_date());
        verify(breweryWeeklyBreakTimeRepository, never()).save(any());
    }

    @Test
    @DisplayName("양조장 일정 변경은 휴게 시작과 종료 중 하나만 입력되면 거부한다")
    void update_brewery_schedule_rejects_partial_break_time() {
        Long userId = 1L;
        ReqUpdateBreweryScheduleDto dto = updateScheduleDto();
        dto.getSchedules().getFirst().setBreak_start(LocalTime.of(12, 0));
        Brewery brewery = mock(Brewery.class);
        given(breweryRepository.findByUserId(userId)).willReturn(Optional.of(brewery));

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> breweryService.updateBrewerySchedule(userId, dto)
        );

        assertEquals(ApplicationError.BREWERY_OPENING_TIME_INVALID, exception.getApplicationError());
        verify(breweryWeeklyOpenTimeRepository, never()).save(any());
        verify(breweryWeeklyBreakTimeRepository, never()).save(any());
    }

    private ReqUpdateBreweryScheduleDto updateScheduleDto() {
        BreweryScheduleDto schedule = new BreweryScheduleDto();
        schedule.setDay_of_week(DayOfWeek.Mon);
        schedule.setOpen_time(LocalTime.of(9, 0));
        schedule.setClose_time(LocalTime.of(18, 0));

        ReqUpdateBreweryScheduleDto dto = new ReqUpdateBreweryScheduleDto();
        dto.setEffective_date(LocalDate.now().plusDays(1));
        dto.setSchedules(List.of(schedule));
        return dto;
    }
}
