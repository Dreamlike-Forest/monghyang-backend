package com.example.monghyang.domain.joy.service;

import com.example.monghyang.domain.global.DayOfWeek;
import com.example.monghyang.domain.global.ClosedStatus;
import com.example.monghyang.domain.global.advice.ApplicationError;
import com.example.monghyang.domain.global.advice.ApplicationException;
import com.example.monghyang.domain.joy.dto.slot.*;
import com.example.monghyang.domain.joy.entity.Joy;
import com.example.monghyang.domain.joy.entity.JoySlot;
import com.example.monghyang.domain.joy.entity.JoyWeeklyStartTime;
import com.example.monghyang.domain.joy.entity.JoyClosedDate;
import com.example.monghyang.domain.joy.entity.JoyClosedStartTime;
import com.example.monghyang.domain.brewery.entity.BreweryWeeklyBreakTime;
import com.example.monghyang.domain.brewery.entity.BreweryWeeklyOpenTime;
import com.example.monghyang.domain.brewery.entity.BreweryClosedDate;
import com.example.monghyang.domain.joy.repository.*;
import com.example.monghyang.domain.brewery.repository.BreweryWeeklyBreakTimeRepository;
import com.example.monghyang.domain.brewery.repository.BreweryWeeklyOpenTimeRepository;
import com.example.monghyang.domain.brewery.repository.BreweryClosedDateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class JoySlotService {
    // Spring AOP: Transactional 적용을 위해 클래스를 의도적으로 분리합니다.
    private final JoySlotRepository joySlotRepository;
    private final JoyRepository joyRepository;
    private final JoyWeeklyStartTimeRepository joyWeeklyStartTimeRepository;
    private final BreweryWeeklyOpenTimeRepository breweryWeeklyOpenTimeRepository;
    private final BreweryWeeklyBreakTimeRepository breweryWeeklyBreakTimeRepository;
    private final BreweryClosedDateRepository breweryClosedDateRepository;
    private final JoyClosedDateRepository joyClosedDateRepository;
    private final JoyClosedStartTimeRepository joyClosedStartTimeRepository;

    /**
     * JoySlot Insert: 새 트랜잭션 생성(REQUIRES_NEW) 전략 적용 -> JDBC 예외 대응
     * @param joyId
     * @param date
     * @param time
     * @param incCount 예약 인원 수
     * @param maxCount 해당 체험 최대 예약 가능 인원수
     * @return
     * @throws DataIntegrityViolationException
     */
    @Transactional
    public void reservationJoySlot(Long joyId, LocalDate date, LocalTime time, Integer incCount, Integer maxCount) throws DataIntegrityViolationException {
        // JDBC 예외 발생으로 인한 트랜잭션 강제 롤백을 insert 수행에 대해서만 적용되도록 하기 위해 트랜잭션 분리

        int ret =  joySlotRepository.upsertJoySlot(joyId, date, time, incCount, maxCount);
        if(ret == 0) {
            throw new ApplicationException(ApplicationError.JOY_COUNT_OVER);
        } else if(ret == 2) {
            log.info("체험 예약 슬롯 생성 upsert 발생. 체험 식별자: {}, 예약 일자: {}, 예약 시간대: {}, 예약 인원: {}", joyId, date, time, incCount);
        }

    }

    /**
     * joy slot 키운트 감소(예약 취소 등)
     * @param joyId
     * @param date
     * @param time
     * @param count
     */
    @Transactional
    public void decrementJoySlotCount(Long joyId, LocalDate date, LocalTime time, Integer count) {
        int ret = joySlotRepository.decrementJoySlotCount(joyId, date, time, count);
        if(ret == 0) {
            // 차감이 불가한 경우 정합성이 깨진 것이므로, 예외 반환 및 로그 기록
            log.info("DB의 체험 예약 인원수 정합성이 깨졌습니다. 체험 식별자: {}, 체험 일자: {}, 체험 시간대: {}, 차감하려는 인원수: {}", joyId, date, time, count);
            throw new ApplicationException(ApplicationError.JOY_DB_COUNT_INVALID);
        }
    }

    /**
     * 특정 날짜와 요일에 해당하는 양조장의 운영 시간 정보(BreweryWeeklyOpenTime)를 전체 스냅샷 이력 중에서 조회합니다.
     * 해당 날짜와 같거나 이전인 effectiveDate를 가진 스냅샷 중 가장 최근의 설정을 반환합니다.
     *
     * @param wotList   양조장의 전체 운영 시간대 스냅샷 리스트
     * @param date      예약 대상 날짜
     * @param dayOfWeek 예약 대상 요일
     * @return 유효한 양조장 운영 시간대 정보 (존재하지 않을 시 null)
     */
    private BreweryWeeklyOpenTime findActiveOpenTime(List<BreweryWeeklyOpenTime> wotList, LocalDate date, DayOfWeek dayOfWeek) {
        return wotList.stream()
                .filter(w -> w.getDayOfWeek() == dayOfWeek && !w.getEffectiveDate().isAfter(date))
                .max(Comparator.comparing(BreweryWeeklyOpenTime::getEffectiveDate))
                .orElse(null);
    }

    /**
     * 특정 날짜와 요일에 해당하는 체험 시작 시간대 목록(JoyWeeklyStartTime)을 전체 스냅샷 이력 중에서 조회합니다.
     * 해당 날짜와 같거나 이전인 effectiveDate를 가진 스냅샷 중 가장 최근의 버전 그룹을 반환합니다.
     *
     * @param jwstList  체험의 전체 일정 시작 시간대 스냅샷 리스트
     * @param date      예약 대상 날짜
     * @param dayOfWeek 예약 대상 요일
     * @return 유효한 체험 시작 시간대 리스트
     */
    private List<JoyWeeklyStartTime> findActiveStartTimes(List<JoyWeeklyStartTime> jwstList, LocalDate date, DayOfWeek dayOfWeek) {
        List<JoyWeeklyStartTime> candidates = jwstList.stream()
                .filter(jw -> jw.getDayOfWeek() == dayOfWeek && !jw.getEffectiveDate().isAfter(date))
                .toList();
        if (candidates.isEmpty()) {
            return List.of();
        }
        LocalDate maxEffectiveDate = candidates.stream()
                .map(JoyWeeklyStartTime::getEffectiveDate)
                .max(LocalDate::compareTo)
                .orElseThrow();
        return candidates.stream()
                .filter(jw -> jw.getEffectiveDate().equals(maxEffectiveDate))
                .toList();
    }

    /**
     * 특정 날짜와 요일에 해당하는 양조장 휴게시간 목록을 전체 스냅샷 이력 중에서 조회합니다.
     *
     * @param breakTimeList 양조장 휴게시간 스냅샷 리스트
     * @param date          예약 대상 날짜
     * @param dayOfWeek     예약 대상 요일
     * @return 해당 날짜에 유효한 휴게시간 목록
     */
    private List<BreweryWeeklyBreakTime> findActiveBreakTimes(List<BreweryWeeklyBreakTime> breakTimeList, LocalDate date, DayOfWeek dayOfWeek) {
        List<BreweryWeeklyBreakTime> candidates = breakTimeList.stream()
                .filter(b -> b.getDayOfWeek() == dayOfWeek && !b.getEffectiveDate().isAfter(date))
                .toList();
        if (candidates.isEmpty()) {
            return List.of();
        }
        LocalDate maxEffectiveDate = candidates.stream()
                .map(BreweryWeeklyBreakTime::getEffectiveDate)
                .max(LocalDate::compareTo)
                .orElseThrow();
        return candidates.stream()
                .filter(b -> b.getEffectiveDate().equals(maxEffectiveDate))
                .toList();
    }

    /**
     * 체험 진행 시간이 양조장 휴게시간과 겹치는지 확인합니다.
     *
     * @param startTime  체험 시작 시간
     * @param timeUnit   체험 진행 시간 단위
     * @param breakTimes 해당 날짜의 유효 휴게시간 목록
     * @return 휴게시간과 겹치면 true
     */
    private boolean overlapsBreakTime(LocalTime startTime, Integer timeUnit, List<BreweryWeeklyBreakTime> breakTimes) {
        LocalTime endTime = startTime.plusMinutes(timeUnit);
        return breakTimes.stream()
                .anyMatch(b -> startTime.isBefore(b.getBreakEnd()) && endTime.isAfter(b.getBreakStart()));
    }

    /**
     * 특정 달의 예약 불가능한 날 조회
     * @param dto ReqFindJoySlotDateDto: joyId, year, month
     * @return 예약 불가 날짜 목록 DTO
     */
    public ResJoySlotDateDto getImpossibleDate(ReqFindJoySlotDateDto dto) {
        // [단계 1] 대상 체험 및 연관된 양조장 조회
        Joy joy = joyRepository.findById(dto.getJoyId()).orElseThrow(() ->
                new ApplicationException(ApplicationError.JOY_NOT_FOUND));
        Long breweryId = joy.getBrewery().getId();

        // [단계 2] 조회하고자 하는 특정 월 범위(1일 ~ 다음달 1일 미포함) 계산
        LocalDate startDate = LocalDate.of(dto.getYear(), dto.getMonth(), 1);
        LocalDate endDate = startDate.plusMonths(1);

        // [단계 3] 특정 월의 계산에 필요한 스냅샷 및 휴무일, 예약 슬롯 정보를 벌크 일괄 조회
        // 이때 스냅샷은 특정 월의 시작일 시점 유효한 것부터 종료일 이전 적용된 것까지 필터링하여 조회합니다.
        List<BreweryWeeklyOpenTime> wotList = breweryWeeklyOpenTimeRepository.findActiveAndFutureOpenTimesInMonth(breweryId, startDate, endDate);
        List<BreweryWeeklyBreakTime> breakTimeList = breweryWeeklyBreakTimeRepository.findActiveAndFutureBreakTimesInMonth(breweryId, startDate, endDate);
        List<JoyWeeklyStartTime> jwstList = joyWeeklyStartTimeRepository.findActiveAndFutureStartTimesInMonth(dto.getJoyId(), startDate, endDate);
        List<BreweryClosedDate> bcdList = breweryClosedDateRepository.findConfirmedByBreweryIdAndMonth(
                breweryId, startDate, endDate, ClosedStatus.CONFIRMED);
        List<JoyClosedDate> jcdList = joyClosedDateRepository.findConfirmedByJoyIdAndMonth(
                dto.getJoyId(), startDate, endDate, ClosedStatus.CONFIRMED);
        List<JoyClosedStartTime> jcstList = joyClosedStartTimeRepository.findConfirmedByJoyIdAndMonth(
                dto.getJoyId(), startDate, endDate, ClosedStatus.CONFIRMED);
        List<FullJoySlotTimeInfoDto> unavailableSlotTimes = joySlotRepository.findUnavailableJoySlotTimesByJoyIdAndMonth(
                dto.getJoyId(), startDate, endDate);

        // [단계 4] 자바 메모리 상에서 빠른 O(1) 조회를 위해 데이터를 Set 및 Map 구조로 캐싱
        Set<LocalDate> breweryClosedDates = bcdList.stream()
                .map(BreweryClosedDate::getClosedDate)
                .collect(Collectors.toSet());

        Set<LocalDate> joyAllDayClosedDates = jcdList.stream()
                .filter(JoyClosedDate::getIsAllDay)
                .map(JoyClosedDate::getClosedDate)
                .collect(Collectors.toSet());

        Map<LocalDate, Set<LocalTime>> closedTimesMap = jcstList.stream()
                .collect(Collectors.groupingBy(
                        jcst -> jcst.getJoyClosedDate().getClosedDate(),
                        Collectors.mapping(JoyClosedStartTime::getClosedStartTime, Collectors.toSet())
                ));

        Map<LocalDate, Set<LocalTime>> fullSlotTimesMap = unavailableSlotTimes.stream()
                .collect(Collectors.groupingBy(
                        FullJoySlotTimeInfoDto::getReservationDate,
                        Collectors.mapping(FullJoySlotTimeInfoDto::getReservationTime, Collectors.toSet())
                ));

        ResJoySlotDateDto result = new ResJoySlotDateDto();

        // [단계 5] 한 달의 모든 날짜(1일~말일)를 순회하며 예약 불가능 여부를 조건별로 검증
        for (LocalDate date = startDate; date.isBefore(endDate); date = date.plusDays(1)) {
            // [단계 5-1] 양조장 임시 휴무일인 경우 -> 예약 불가
            if (breweryClosedDates.contains(date)) {
                result.getJoy_unavailable_reservation_date().add(date);
                continue;
            }

            // [단계 5-2] 체험 임시 전면(All Day) 휴무일인 경우 -> 예약 불가
            if (joyAllDayClosedDates.contains(date)) {
                result.getJoy_unavailable_reservation_date().add(date);
                continue;
            }

            DayOfWeek dayOfWeek = DayOfWeek.from(date.getDayOfWeek());

            // [단계 5-3] 해당 날짜 및 요일에 부합하는 유효한 양조장 운영시간 스냅샷 매칭
            BreweryWeeklyOpenTime openTimeInfo = findActiveOpenTime(wotList, date, dayOfWeek);
            if (openTimeInfo == null || openTimeInfo.getOpenTime() == null || openTimeInfo.getCloseTime() == null) {
                result.getJoy_unavailable_reservation_date().add(date);
                continue;
            }

            // [단계 5-4] 해당 날짜 및 요일에 부합하는 유효한 체험 시작 시간대 스냅샷 매칭
            List<JoyWeeklyStartTime> startTimes = findActiveStartTimes(jwstList, date, dayOfWeek);
            if (startTimes.isEmpty()) {
                result.getJoy_unavailable_reservation_date().add(date);
                continue;
            }

            // [단계 5-5] 양조장 운영 시간 범위(openTime <= startTime < closeTime) 내에 위치한 활성 체험 슬롯만 추출 (교집합)
            LocalTime openTime = openTimeInfo.getOpenTime();
            LocalTime closeTime = openTimeInfo.getCloseTime();
            List<BreweryWeeklyBreakTime> activeBreakTimes = findActiveBreakTimes(breakTimeList, date, dayOfWeek);
            List<LocalTime> activeSlots = startTimes.stream()
                    .map(JoyWeeklyStartTime::getStartTime)
                    .filter(t -> !t.isBefore(openTime) && !t.plusMinutes(joy.getTimeUnit()).isAfter(closeTime))
                    .filter(t -> !overlapsBreakTime(t, joy.getTimeUnit(), activeBreakTimes))
                    .toList();

            if (activeSlots.isEmpty()) {
                result.getJoy_unavailable_reservation_date().add(date);
                continue;
            }

            // [단계 5-6] 당일 시간대별 체험 휴무(JoyClosedStartTime)에 걸리는 슬롯 제거 (차집합)
            Set<LocalTime> closedTimes = closedTimesMap.getOrDefault(date, Set.of());
            List<LocalTime> validSlots = activeSlots.stream()
                    .filter(t -> !closedTimes.contains(t))
                    .toList();

            if (validSlots.isEmpty()) {
                result.getJoy_unavailable_reservation_date().add(date);
                continue;
            }

            // [단계 5-7] 매진 여부 검증: 모든 유효 슬롯이 예약 인원 한도에 도달하면 예약 불가
            Set<LocalTime> fullSlotTimes = fullSlotTimesMap.getOrDefault(date, Set.of());
            boolean allValidSlotsFull = validSlots.stream().allMatch(fullSlotTimes::contains);

            if (allValidSlotsFull) {
                result.getJoy_unavailable_reservation_date().add(date);
            }
        }
        return result;
    }

    /**
     * 특정 날의 각 시간대의 '남아있는 자릿수' 리스트를 반환
     * @param joyId Long
     * @param targetDate 특정 날 LocalDate
     * @return 남아있는 자릿수가 0이라면 예약 불가를 의미
     */
    public ResJoySlotTimeDto getRemainingCountList(Long joyId, LocalDate targetDate) {
        ResJoySlotTimeDto result = new ResJoySlotTimeDto();
        Joy joy = joyRepository.findById(joyId).orElseThrow(() ->
                new ApplicationException(ApplicationError.JOY_NOT_FOUND));

        DayOfWeek dayOfWeek = DayOfWeek.from(targetDate.getDayOfWeek());

        // 1. 해당 일자의 양조장 운영시간 스냅샷 조회
        LocalDate limitDate = targetDate.plusDays(1);
        List<BreweryWeeklyOpenTime> wotList = breweryWeeklyOpenTimeRepository.findActiveAndFutureOpenTimesInMonth(joy.getBrewery().getId(), targetDate, limitDate);
        BreweryWeeklyOpenTime openTimeInfo = findActiveOpenTime(wotList, targetDate, dayOfWeek);
        List<BreweryWeeklyBreakTime> breakTimeList = breweryWeeklyBreakTimeRepository.findActiveAndFutureBreakTimesInMonth(joy.getBrewery().getId(), targetDate, limitDate);
        List<BreweryWeeklyBreakTime> activeBreakTimes = findActiveBreakTimes(breakTimeList, targetDate, dayOfWeek);

        // 2. 해당 일자의 체험 시작 시간대 스냅샷 조회
        List<JoyWeeklyStartTime> jwstList = joyWeeklyStartTimeRepository.findActiveAndFutureStartTimesInMonth(joyId, targetDate, limitDate);
        List<JoyWeeklyStartTime> startTimes = findActiveStartTimes(jwstList, targetDate, dayOfWeek);

        if (openTimeInfo != null && openTimeInfo.getOpenTime() != null && openTimeInfo.getCloseTime() != null) {
            LocalTime openTime = openTimeInfo.getOpenTime();
            LocalTime closeTime = openTimeInfo.getCloseTime();

            // 3. 양조장 운영 시간 범위 내에 속하는 활성 체험 시작 시간대들을 정렬하여 응답 필드에 추가
            List<LocalTime> activeStartTimes = startTimes.stream()
                    .map(JoyWeeklyStartTime::getStartTime)
                    .filter(t -> !t.isBefore(openTime) && !t.plusMinutes(joy.getTimeUnit()).isAfter(closeTime))
                    .filter(t -> !overlapsBreakTime(t, joy.getTimeUnit(), activeBreakTimes))
                    .sorted()
                    .toList();

            result.getTime_info().addAll(activeStartTimes);
        }

        // 4. 시간대별 남아있는 자리 정보를 DTO 필드에 추가
        Set<LocalTime> responseTimes = Set.copyOf(result.getTime_info());
        List<JoySlot> joySlotList = joySlotRepository.findByJoyIdAndDate(joyId, targetDate);
        for (JoySlot joySlot : joySlotList) {
            if (!responseTimes.contains(joySlot.getReservationTime())) {
                continue;
            }
            result.getRemaining_count_list().add(JoySlotTimeCountDto.timeCountOf(
                    joySlot.getReservationTime(),
                    joy.getMaxCount() - joySlot.getCount()
            ));
        }
        return result;
    }

}
