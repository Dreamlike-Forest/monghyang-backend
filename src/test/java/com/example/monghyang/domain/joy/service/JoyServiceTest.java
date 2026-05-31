package com.example.monghyang.domain.joy.service;

import com.example.monghyang.domain.brewery.entity.Brewery;
import com.example.monghyang.domain.brewery.entity.BreweryWeeklyBreakTime;
import com.example.monghyang.domain.brewery.entity.RegionType;
import com.example.monghyang.domain.brewery.repository.BreweryRepository;
import com.example.monghyang.domain.brewery.repository.BreweryWeeklyBreakTimeRepository;
import com.example.monghyang.domain.global.DayOfWeek;
import com.example.monghyang.domain.global.advice.ApplicationError;
import com.example.monghyang.domain.global.advice.ApplicationException;
import com.example.monghyang.domain.image.service.StorageService;
import com.example.monghyang.domain.joy.dto.JoyScheduleDto;
import com.example.monghyang.domain.joy.dto.ReqJoyDto;
import com.example.monghyang.domain.joy.dto.ReqUpdateJoyScheduleDto;
import com.example.monghyang.domain.joy.dto.ResJoyDto;
import com.example.monghyang.domain.joy.entity.Joy;
import com.example.monghyang.domain.joy.entity.JoyWeeklyStartTime;
import com.example.monghyang.domain.joy.repository.JoyRepository;
import com.example.monghyang.domain.joy.repository.JoyWeeklyStartTimeRepository;
import com.example.monghyang.domain.users.entity.Role;
import com.example.monghyang.domain.users.entity.RoleType;
import com.example.monghyang.domain.users.entity.Users;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class JoyServiceTest {
    @Mock
    JoyRepository joyRepository;
    @Mock
    BreweryRepository breweryRepository;
    @Mock
    StorageService storageService;
    @Mock
    JoyWeeklyStartTimeRepository joyWeeklyStartTimeRepository;
    @Mock
    JoyOrderService joyOrderService;
    @Mock
    BreweryWeeklyBreakTimeRepository breweryWeeklyBreakTimeRepository;
    @InjectMocks
    JoyService joyService;

    @Test
    @DisplayName("내 체험 목록 조회는 삭제되지 않은 체험만 반환한다")
    void get_my_joy_list_returns_active_joys_only() {
        Long userId = 1L;
        Brewery brewery = brewery();
        Joy joy = joy(brewery);
        given(joyRepository.findActiveByUserId(userId)).willReturn(List.of(joy));

        List<ResJoyDto> result = joyService.getMyJoyList(userId);

        assertEquals(1, result.size());
        verify(joyRepository).findActiveByUserId(userId);
    }

    @Test
    @DisplayName("이미 삭제된 체험은 다시 삭제할 수 없다")
    void delete_joy_rejects_already_deleted_joy() {
        Long userId = 1L;
        Long joyId = 10L;
        Brewery brewery = brewery();
        ReflectionTestUtils.setField(brewery, "id", 5L);
        given(breweryRepository.findByUserId(userId)).willReturn(Optional.of(brewery));
        given(joyRepository.findActiveByBreweryIdAndJoyId(5L, joyId)).willReturn(Optional.empty());

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> joyService.deleteJoy(userId, joyId)
        );

        assertEquals(ApplicationError.JOY_NOT_FOUND, exception.getApplicationError());
    }

    @Test
    @DisplayName("삭제된 체험 복구는 삭제 상태 체험만 대상으로 한다")
    void restore_joy_uses_deleted_joy_lookup() {
        Long userId = 1L;
        Long joyId = 10L;
        Brewery brewery = brewery();
        Joy joy = joy(brewery);
        joy.setDeleted();
        ReflectionTestUtils.setField(brewery, "id", 5L);
        given(breweryRepository.findByUserId(userId)).willReturn(Optional.of(brewery));
        given(joyRepository.findDeletedByBreweryIdAndJoyId(5L, joyId)).willReturn(Optional.of(joy));

        joyService.restoreJoy(userId, joyId);

        assertEquals(false, joy.getIsDeleted());
        verify(joyRepository).save(joy);
    }

    @Test
    @SuppressWarnings("unchecked")
    @DisplayName("체험 생성 시 요청한 요일별 시작 시간을 오늘 적용 스냅샷으로 저장한다")
    void create_joy_saves_initial_weekly_start_time_snapshot() {
        Long userId = 1L;
        Brewery brewery = brewery();
        ReqJoyDto dto = reqJoyDto();
        given(breweryRepository.findByUserId(userId)).willReturn(Optional.of(brewery));

        joyService.createJoy(userId, dto);

        ArgumentCaptor<List<JoyWeeklyStartTime>> captor = ArgumentCaptor.forClass(List.class);
        verify(joyWeeklyStartTimeRepository).saveAll(captor.capture());
        List<JoyWeeklyStartTime> saved = captor.getValue();
        assertEquals(3, saved.size());
        assertEquals(LocalDate.now(), saved.getFirst().getEffectiveDate());
        assertEquals(DayOfWeek.Mon, saved.getFirst().getDayOfWeek());
        assertEquals(LocalTime.of(10, 0), saved.getFirst().getStartTime());
    }

    @Test
    @SuppressWarnings("unchecked")
    @DisplayName("체험 일정 변경 시 같은 적용일 스냅샷을 교체하고 환불 요청 처리를 호출한다")
    void update_joy_schedule_replaces_snapshot_and_requests_refund() {
        Long userId = 1L;
        Long joyId = 10L;
        LocalDate effectiveDate = LocalDate.now().plusDays(1);
        Brewery brewery = brewery();
        Joy joy = joy(brewery);
        ReflectionTestUtils.setField(brewery, "id", 5L);
        ReqUpdateJoyScheduleDto dto = reqUpdateJoyScheduleDto(joyId, effectiveDate);
        given(breweryRepository.findByUserId(userId)).willReturn(Optional.of(brewery));
        given(joyRepository.findActiveByBreweryIdAndJoyId(5L, joyId)).willReturn(Optional.of(joy));

        joyService.updateJoySchedule(userId, dto);

        verify(joyWeeklyStartTimeRepository).deleteByJoyIdAndEffectiveDate(joyId, effectiveDate);
        ArgumentCaptor<List<JoyWeeklyStartTime>> captor = ArgumentCaptor.forClass(List.class);
        verify(joyWeeklyStartTimeRepository).saveAll(captor.capture());
        assertEquals(2, captor.getValue().size());
        verify(joyOrderService).setRefundRequestedByJoyScheduleChange(joyId, effectiveDate);
    }

    @Test
    @DisplayName("체험 일정 변경 요청에 중복 요일이 있으면 INVALID_TIME 예외가 발생한다")
    void update_joy_schedule_rejects_duplicate_day_of_week() {
        Long userId = 1L;
        Long joyId = 10L;
        LocalDate effectiveDate = LocalDate.now().plusDays(1);
        Brewery brewery = brewery();
        ReflectionTestUtils.setField(brewery, "id", 5L);
        ReqUpdateJoyScheduleDto dto = reqUpdateJoyScheduleDto(joyId, effectiveDate);
        dto.setSchedules(List.of(
                schedule(DayOfWeek.Mon, LocalTime.of(10, 0)),
                schedule(DayOfWeek.Mon, LocalTime.of(11, 0))
        ));
        given(breweryRepository.findByUserId(userId)).willReturn(Optional.of(brewery));
        given(joyRepository.findActiveByBreweryIdAndJoyId(5L, joyId)).willReturn(Optional.of(joy(brewery)));

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> joyService.updateJoySchedule(userId, dto)
        );

        assertEquals(ApplicationError.INVALID_TIME, exception.getApplicationError());
    }

    @Test
    @DisplayName("체험 생성 시 양조장 휴게시간과 겹치는 시작 시간이 있으면 요청을 반려한다")
    void create_joy_rejects_start_time_overlapping_break_time() {
        Long userId = 1L;
        Brewery brewery = brewery();
        ReflectionTestUtils.setField(brewery, "id", 5L);
        ReqJoyDto dto = reqJoyDto();
        dto.setSchedules(List.of(schedule(DayOfWeek.Mon, LocalTime.of(12, 0))));
        given(breweryRepository.findByUserId(userId)).willReturn(Optional.of(brewery));
        given(breweryWeeklyBreakTimeRepository.findActiveBreakTimesByBreweryIdAndDate(5L, LocalDate.now(), DayOfWeek.Mon))
                .willReturn(List.of(breakTime(LocalTime.of(12, 0), LocalTime.of(13, 0))));

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> joyService.createJoy(userId, dto)
        );

        assertEquals(ApplicationError.INVALID_TIME, exception.getApplicationError());
    }

    @Test
    @DisplayName("체험 일정 변경 시 양조장 휴게시간과 겹치는 시작 시간이 있으면 요청을 반려한다")
    void update_joy_schedule_rejects_start_time_overlapping_break_time() {
        Long userId = 1L;
        Long joyId = 10L;
        LocalDate effectiveDate = LocalDate.now().plusDays(1);
        Brewery brewery = brewery();
        ReflectionTestUtils.setField(brewery, "id", 5L);
        Joy joy = joy(brewery);
        ReqUpdateJoyScheduleDto dto = reqUpdateJoyScheduleDto(joyId, effectiveDate);
        dto.setSchedules(List.of(schedule(DayOfWeek.Mon, LocalTime.of(12, 0))));
        given(breweryRepository.findByUserId(userId)).willReturn(Optional.of(brewery));
        given(joyRepository.findActiveByBreweryIdAndJoyId(5L, joyId)).willReturn(Optional.of(joy));
        given(breweryWeeklyBreakTimeRepository.findActiveBreakTimesByBreweryIdAndDate(5L, effectiveDate, DayOfWeek.Mon))
                .willReturn(List.of(breakTime(LocalTime.of(12, 0), LocalTime.of(13, 0))));

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> joyService.updateJoySchedule(userId, dto)
        );

        assertEquals(ApplicationError.INVALID_TIME, exception.getApplicationError());
    }

    private ReqJoyDto reqJoyDto() {
        ReqJoyDto dto = new ReqJoyDto();
        dto.setName("막걸리 빚기");
        dto.setPlace("체험장");
        dto.setDetail("체험 설명");
        dto.setTime_unit(60);
        dto.setOrigin_price(BigDecimal.valueOf(10000));
        dto.setMax_count(10);
        dto.setSchedules(List.of(
                schedule(DayOfWeek.Mon, LocalTime.of(10, 0), LocalTime.of(14, 0)),
                schedule(DayOfWeek.Tue, LocalTime.of(11, 0))
        ));
        return dto;
    }

    private ReqUpdateJoyScheduleDto reqUpdateJoyScheduleDto(Long joyId, LocalDate effectiveDate) {
        ReqUpdateJoyScheduleDto dto = new ReqUpdateJoyScheduleDto();
        dto.setJoyId(joyId);
        dto.setEffective_date(effectiveDate);
        dto.setSchedules(List.of(schedule(DayOfWeek.Mon, LocalTime.of(10, 0), LocalTime.of(14, 0))));
        return dto;
    }

    private JoyScheduleDto schedule(DayOfWeek dayOfWeek, LocalTime... startTimes) {
        JoyScheduleDto dto = new JoyScheduleDto();
        dto.setDay_of_week(dayOfWeek);
        dto.setStart_times(List.of(startTimes));
        return dto;
    }

    private Brewery brewery() {
        Role role = new Role();
        role.setName(RoleType.ROLE_BREWERY);
        Users users = Users.generalBuilder()
                .role(role)
                .email("brewery@example.com")
                .password("password")
                .nickname("brewery")
                .name("양조장")
                .phone("01012345678")
                .birth(LocalDate.of(1990, 1, 1))
                .gender(true)
                .address("주소")
                .address_detail("상세 주소")
                .isAgreed(true)
                .build();
        return Brewery.breweryBuilder()
                .user(users)
                .regionType(RegionType.nameFrom("서울"))
                .breweryName("양조장")
                .breweryAddress("주소")
                .breweryAddressDetail("상세 주소")
                .businessRegistrationNumber("123")
                .breweryDepositor("예금주")
                .breweryAccountNumber("123")
                .breweryBankName("은행")
                .isRegularVisit(true)
                .isAgreedBrewery(true)
                .build();
    }

    private Joy joy(Brewery brewery) {
        return Joy.joyBuilder()
                .brewery(brewery)
                .name("막걸리 빚기")
                .place("체험장")
                .detail("체험 설명")
                .originPrice(BigDecimal.valueOf(10000))
                .timeUnit(60)
                .maxCount(10)
                .minCount(1)
                .build();
    }

    private BreweryWeeklyBreakTime breakTime(LocalTime breakStart, LocalTime breakEnd) {
        return BreweryWeeklyBreakTime.builder()
                .brewery(brewery())
                .dayOfWeek(DayOfWeek.Mon)
                .breakStart(breakStart)
                .breakEnd(breakEnd)
                .effectiveDate(LocalDate.now())
                .build();
    }
}
