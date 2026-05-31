package com.example.monghyang.domain.joy.service;

import com.example.monghyang.domain.joy.dto.ReqJoyDto;
import com.example.monghyang.domain.joy.dto.JoyScheduleDto;
import com.example.monghyang.domain.joy.dto.ReqUpdateJoyScheduleDto;
import com.example.monghyang.domain.joy.dto.ReqUpdateJoyDto;
import com.example.monghyang.domain.joy.dto.ResJoyDto;
import com.example.monghyang.domain.joy.entity.Joy;
import com.example.monghyang.domain.joy.entity.JoyWeeklyStartTime;
import com.example.monghyang.domain.joy.repository.JoyRepository;
import com.example.monghyang.domain.brewery.entity.Brewery;
import com.example.monghyang.domain.brewery.entity.BreweryWeeklyBreakTime;
import com.example.monghyang.domain.brewery.repository.BreweryRepository;
import com.example.monghyang.domain.brewery.repository.BreweryWeeklyBreakTimeRepository;
import com.example.monghyang.domain.global.DayOfWeek;
import com.example.monghyang.domain.global.advice.ApplicationError;
import com.example.monghyang.domain.global.advice.ApplicationException;
import com.example.monghyang.domain.joy.repository.JoyWeeklyStartTimeRepository;
import com.example.monghyang.domain.image.service.ImageType;
import com.example.monghyang.domain.image.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class JoyService {
    private final JoyRepository joyRepository;
    private final BreweryRepository breweryRepository;
    private final StorageService storageService;
    private final JoyWeeklyStartTimeRepository joyWeeklyStartTimeRepository;
    private final JoyOrderService joyOrderService;
    private final BreweryWeeklyBreakTimeRepository breweryWeeklyBreakTimeRepository;

    // 체험 등록
    @Transactional
    public void createJoy(Long userId, ReqJoyDto reqJoyDto) {
        Brewery brewery = breweryRepository.findByUserId(userId).orElseThrow(() ->
                new ApplicationException(ApplicationError.BREWERY_NOT_FOUND));
        validateNotOverlappingBreakTimes(brewery, reqJoyDto.getSchedules(), LocalDate.now(), reqJoyDto.getTime_unit());
        String imageKey = null;
        if(reqJoyDto.getImage() != null) {
            imageKey = storageService.upload(reqJoyDto.getImage(), ImageType.JOY_IMAGE);
        }
        Joy joy = Joy.joyBuilder()
                .brewery(brewery).name(reqJoyDto.getName())
                .place(reqJoyDto.getPlace()).detail(reqJoyDto.getDetail())
                .originPrice(reqJoyDto.getOrigin_price()).timeUnit(reqJoyDto.getTime_unit())
                .imageKey(imageKey).maxCount(reqJoyDto.getMax_count()).minCount(1)
                .build();
        joyRepository.save(joy);
        saveWeeklyStartTimes(joy, reqJoyDto.getSchedules(), LocalDate.now());
        if(brewery.getJoyCount() == 0) {
            brewery.updateMinJoyPrice(joy.getFinalPrice());
        } else if(joy.getFinalPrice().compareTo(brewery.getMinJoyPrice()) < 0){
            // 양조장 최소 체험 가격 갱신 여부 검증 후 갱신
            brewery.updateMinJoyPrice(joy.getFinalPrice());
        }

        brewery.increaseJoyCount(); // 양조장의 체험 개수 카운트 1 증가
    }

    /**
     * 체험 요일별 시작 시간 스냅샷을 새 적용일 기준으로 교체합니다.
     *
     * @param userId 요청한 양조장 회원 식별자
     * @param dto 변경할 체험 일정과 적용일 요청
     */
    @Transactional
    public void updateJoySchedule(Long userId, ReqUpdateJoyScheduleDto dto) {
        Brewery brewery = breweryRepository.findByUserId(userId).orElseThrow(() ->
                new ApplicationException(ApplicationError.BREWERY_NOT_FOUND));
        Joy joy = joyRepository.findActiveByBreweryIdAndJoyId(brewery.getId(), dto.getJoyId()).orElseThrow(() ->
                new ApplicationException(ApplicationError.JOY_NOT_FOUND));
        if(dto.getEffective_date().isBefore(LocalDate.now())) {
            throw new ApplicationException(ApplicationError.INVALID_TIME);
        }
        validateScheduleDuplicates(dto.getSchedules());
        validateNotOverlappingBreakTimes(brewery, dto.getSchedules(), dto.getEffective_date(), joy.getTimeUnit());

        // 같은 적용일 스냅샷은 한 번 삭제한 뒤 요청 전체를 다시 저장해 동일 기준으로 교체한다.
        joyWeeklyStartTimeRepository.deleteByJoyIdAndEffectiveDate(dto.getJoyId(), dto.getEffective_date());
        saveWeeklyStartTimes(joy, dto.getSchedules(), dto.getEffective_date());
        joyOrderService.setRefundRequestedByJoyScheduleChange(dto.getJoyId(), dto.getEffective_date());
    }

    /**
     * 삭제되지 않은 체험만 삭제 처리합니다.
     *
     * @param userId 체험을 관리하는 양조장 회원 식별자
     * @param joyId  삭제할 체험 식별자
     */
    public void deleteJoy(Long userId, Long joyId) {
        Brewery brewery = breweryRepository.findByUserId(userId).orElseThrow(() ->
                new ApplicationException(ApplicationError.BREWERY_NOT_FOUND));
        Joy joy = joyRepository.findActiveByBreweryIdAndJoyId(brewery.getId(), joyId).orElseThrow(() ->
                new ApplicationException(ApplicationError.JOY_NOT_FOUND));
        joy.setDeleted();
        joyRepository.save(joy);
        brewery.decreaseJoyCount(); // 양조장의 체험 개수 카운트 1 감소
    }

    /**
     * 삭제된 체험만 복구 대상으로 조회해 삭제 상태를 해제합니다.
     *
     * @param userId 체험을 관리하는 양조장 회원 식별자
     * @param joyId  복구할 체험 식별자
     */
    public void restoreJoy(Long userId, Long joyId) {
        Brewery brewery = breweryRepository.findByUserId(userId).orElseThrow(() ->
                new ApplicationException(ApplicationError.BREWERY_NOT_FOUND));
        Joy joy = joyRepository.findDeletedByBreweryIdAndJoyId(brewery.getId(), joyId).orElseThrow(() ->
                new ApplicationException(ApplicationError.JOY_NOT_FOUND));
        joy.unSetDeleted();
        joyRepository.save(joy);
    }

    public void setSoldout(Long userId, Long joyId) {
        Brewery brewery = breweryRepository.findByUserId(userId).orElseThrow(() ->
                new ApplicationException(ApplicationError.BREWERY_NOT_FOUND));
        Joy joy = joyRepository.findActiveByBreweryIdAndJoyId(brewery.getId(), joyId).orElseThrow(() ->
                new ApplicationException(ApplicationError.JOY_NOT_FOUND));
        joy.setIsSoldout();
        joyRepository.save(joy);
    }

    public void unSetSoldout(Long userId, Long joyId) {
        Brewery brewery = breweryRepository.findByUserId(userId).orElseThrow(() ->
                new ApplicationException(ApplicationError.BREWERY_NOT_FOUND));
        Joy joy = joyRepository.findActiveByBreweryIdAndJoyId(brewery.getId(), joyId).orElseThrow(() ->
                new ApplicationException(ApplicationError.JOY_NOT_FOUND));
        joy.unSetIsSoldout();
        joyRepository.save(joy);
    }

    // 체험 수정(가격 및 할인율, 기타 체험 정보, 매진 처리 등)
    @Transactional
    public void updateJoy(Long userId, ReqUpdateJoyDto reqUpdateJoyDto) {
        Brewery brewery = breweryRepository.findByUserId(userId).orElseThrow(() ->
                new ApplicationException(ApplicationError.BREWERY_NOT_FOUND));
        Joy joy = joyRepository.findActiveByBreweryIdAndJoyId(brewery.getId(), reqUpdateJoyDto.getId()).orElseThrow(() ->
                new ApplicationException(ApplicationError.JOY_NOT_FOUND));
        if(reqUpdateJoyDto.getImage() != null) {
            storageService.remove(joy.getImageKey());
            String newImageKey = storageService.upload(reqUpdateJoyDto.getImage(), ImageType.JOY_IMAGE);
            joy.updateImageKey(newImageKey);
        }
        if(reqUpdateJoyDto.getName() != null) {
            joy.updateName(reqUpdateJoyDto.getName());
        }
        if(reqUpdateJoyDto.getPlace() != null) {
            joy.updatePlace(reqUpdateJoyDto.getPlace());
        }
        if(reqUpdateJoyDto.getDetail() != null) {
            joy.updateDetail(reqUpdateJoyDto.getDetail());
        }
        if(reqUpdateJoyDto.getOrigin_price() != null) {
            joy.updateOriginPrice(reqUpdateJoyDto.getOrigin_price());
        }
        if(reqUpdateJoyDto.getDiscount_rate() != null) {
            joy.updateDiscountRate(reqUpdateJoyDto.getDiscount_rate());
        }
        if(reqUpdateJoyDto.getIs_soldout() != null) {
            joy.updateSoldout(reqUpdateJoyDto.getIs_soldout());
        }
        if(reqUpdateJoyDto.getTime_unit() != null) {
            joy.updateTimeUnit(reqUpdateJoyDto.getTime_unit());
        }
        if(reqUpdateJoyDto.getMax_count() != null) {
            joy.updateMaxCount(reqUpdateJoyDto.getMax_count());
        }
    }

    /**
     * 자신이 관리하는 삭제되지 않은 체험 전체 조회
     * @param userId 자신의 유저 식별자
     * @return 삭제되지 않은 체험 응답 목록
     */
    public List<ResJoyDto> getMyJoyList(Long userId) {
        List<Joy> joyList = joyRepository.findActiveByUserId(userId);
        if(joyList.isEmpty()) {
            throw new ApplicationException(ApplicationError.JOY_NOT_FOUND);
        }
        return joyList.stream().map(ResJoyDto::joyFrom).toList();
    }

    private void saveWeeklyStartTimes(Joy joy, List<JoyScheduleDto> schedules, LocalDate effectiveDate) {
        validateScheduleDuplicates(schedules);
        List<JoyWeeklyStartTime> weeklyStartTimes = new ArrayList<>();
        for(JoyScheduleDto schedule : schedules) {
            for(LocalTime startTime : schedule.getStart_times()) {
                weeklyStartTimes.add(JoyWeeklyStartTime.joyDayOfWeekStartTimeEffectiveDateOf(
                        joy,
                        schedule.getDay_of_week(),
                        startTime,
                        effectiveDate
                ));
            }
        }
        joyWeeklyStartTimeRepository.saveAll(weeklyStartTimes);
    }

    private void validateScheduleDuplicates(List<JoyScheduleDto> schedules) {
        Set<DayOfWeek> dayOfWeeks = new HashSet<>();
        for(JoyScheduleDto schedule : schedules) {
            if(!dayOfWeeks.add(schedule.getDay_of_week())) {
                throw new ApplicationException(ApplicationError.INVALID_TIME);
            }
            Set<LocalTime> startTimes = new HashSet<>();
            for(LocalTime startTime : schedule.getStart_times()) {
                if(!startTimes.add(startTime)) {
                    throw new ApplicationException(ApplicationError.INVALID_TIME);
                }
            }
        }
    }

    /**
     * 체험 시작 시간 요청이 적용일 기준 양조장 휴게시간과 겹치면 예외를 발생시킵니다.
     *
     * @param brewery       체험이 속한 양조장
     * @param schedules     요청된 요일별 체험 시작 시간
     * @param effectiveDate 체험 일정 적용 시작일
     * @param timeUnit      체험 진행 시간 단위
     */
    private void validateNotOverlappingBreakTimes(Brewery brewery, List<JoyScheduleDto> schedules, LocalDate effectiveDate, Integer timeUnit) {
        for (JoyScheduleDto schedule : schedules) {
            // 요청 요일에 적용되는 양조장 휴게시간 스냅샷을 적용일 기준으로 조회한다.
            List<BreweryWeeklyBreakTime> breakTimes = breweryWeeklyBreakTimeRepository.findActiveBreakTimesByBreweryIdAndDate(
                    brewery.getId(),
                    effectiveDate,
                    schedule.getDay_of_week()
            );
            for (LocalTime startTime : schedule.getStart_times()) {
                // 체험 시작 시간과 진행 시간으로 실제 체험 종료 시간을 계산한다.
                LocalTime endTime = startTime.plusMinutes(timeUnit);
                // 체험 진행 구간이 휴게시간 구간과 하나라도 겹치면 저장할 수 없는 일정으로 판단한다.
                boolean overlapsBreakTime = breakTimes.stream()
                        .anyMatch(b -> startTime.isBefore(b.getBreakEnd()) && endTime.isAfter(b.getBreakStart()));
                if (overlapsBreakTime) {
                    throw new ApplicationException(ApplicationError.INVALID_TIME);
                }
            }
        }
    }
}
