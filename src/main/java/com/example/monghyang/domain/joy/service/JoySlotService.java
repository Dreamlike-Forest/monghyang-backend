package com.example.monghyang.domain.joy.service;

import com.example.monghyang.domain.global.advice.ApplicationError;
import com.example.monghyang.domain.global.advice.ApplicationException;
import com.example.monghyang.domain.joy.dto.*;
import com.example.monghyang.domain.joy.dto.slot.*;
import com.example.monghyang.domain.joy.entity.JoySlot;
import com.example.monghyang.domain.joy.repository.JoyRepository;
import com.example.monghyang.domain.joy.repository.JoySlotRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class JoySlotService {
    // Spring AOP: Transactional 적용을 위해 클래스를 의도적으로 분리합니다.
    private final JoySlotRepository joySlotRepository;
    private final JoyRepository joyRepository;

    @Autowired
    public JoySlotService(JoySlotRepository joySlotRepository, JoyRepository joyRepository) {
        this.joySlotRepository = joySlotRepository;
        this.joyRepository = joyRepository;
    }

    /**
     * JoySlot Insert: 새 트랜잭션 생성(REQUIRES_NEW) 전략 적용 -> JDBC 예외 대응
     * @param joyId
     * @param date
     * @param time
     * @param count
     * @return
     * @throws DataIntegrityViolationException
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int createJoySlot(Long joyId, LocalDate date, LocalTime time, Integer count) throws DataIntegrityViolationException {
        // JDBC 예외 발생으로 인한 트랜잭션 강제 롤백을 insert 수행에 대해서만 적용되도록 하기 위해 트랜잭션 분리

        return joySlotRepository.insertJoySlot(joyId, date, time, count);
    }

    @Transactional
    public int incrementJoySlotCount(Long joyId, LocalDate date, LocalTime time, Integer count) {
        return joySlotRepository.incrementJoySlotCount(joyId, date, time, count);
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
            // 카운트 감소 후 카운트가 0이 되는 경우 감소되지 않는다. (ret == 0)
            // 이 경우 해당 슬롯을 아예 제거한다.
            joySlotRepository.deleteJoySlot(joyId, date, time);
        }
    }

    /**
     * 특정 달의 예약 불가능한 날 조회
     * @param dto ReqFindJoySlotDateDto: joyId, year, month
     * @return
     */
    public ResJoySlotDateDto getImpossibleDate(ReqFindJoySlotDateDto dto) {
        // 대상 체험이 하루 몇 회 운영되는지 계산하기 위한 (체험 시간 단위, 양조장 영업 시작 시간/종료 시간) 정보 조회
        JoyScheduleCountDto scheduleCountDto = joySlotRepository.findJoyScheduleCountByJoyId(dto.getJoyId()).orElseThrow(() ->
                new ApplicationException(ApplicationError.JOY_NOT_FOUND));
        // 하루 몇 회 운영되는지 계산: count로 저장
        long count = Duration.between(scheduleCountDto.getStartTime(), scheduleCountDto.getEndTime()).toMinutes() / scheduleCountDto.getTimeUnit();

        // 대상 month 내의 기간 중 day 별로 '예약 불가능한 시간대'의 개수를 카운팅
        // ex: (1일, 1), (2일, 10) == 1일의 예약 불가능 시간대: 1개, 2일의 예약 불가능 시간대: 10개
        LocalDate startDate = LocalDate.of(dto.getYear(), dto.getMonth(), 1);
        LocalDate endDate = startDate.plusMonths(1);
        List<UnavailableJoySlotTimeCountDto> dateInfoList = joySlotRepository.findUnavailableJoySlotTimeCountByJoyIdAndMonth(
                dto.getJoyId(),
                startDate,
                endDate
        );

        // 예약 불가능한 시간대의 개수가 위의 count 값과 같거나 더 큰 경우: 예약 불가능한 날
        // 반환되는 dto에 해당 날짜 정보 삽입
        ResJoySlotDateDto result = new ResJoySlotDateDto();
        for(UnavailableJoySlotTimeCountDto countDto : dateInfoList) {
            if(countDto.getCount() >= count) {
                result.getJoy_unavailable_reservation_date().add(countDto.getReservationDate());
            }
        }
        return result;
    }

    /**
     * 특정 날의 각 시간대의 '남이있는 자릿수' 리스트를 반환
     * @param joyId Long
     * @param targetDate 특정 날 LocalDate
     * @return 남아있는 자릿수가 0이라면 예약 불가를 의미
     */
    public ResJoySlotTimeDto getRemainingCountList(Long joyId, LocalDate targetDate) {
        ResJoySlotTimeDto result = new ResJoySlotTimeDto();
        JoyScheduleCountDto info = joySlotRepository.findJoyScheduleCountByJoyId(joyId).orElseThrow(() ->
                new ApplicationException(ApplicationError.JOY_NOT_FOUND));

        // 운영 시간대 정보를 response dto의 필드에 추가
        LocalTime breweryStartTime = info.getStartTime();
        LocalTime breweryEndTime = info.getEndTime();
        while(breweryStartTime.isBefore(breweryEndTime)) {
            result.getTime_info().add(breweryStartTime);
            breweryStartTime = breweryStartTime.plusMinutes(info.getTimeUnit());
        }

        // 시간대별 남아있는 자리 정보를 response dto 필드에 추가
        List<JoySlot> joySlotList = joySlotRepository.findByJoyIdAndDate(joyId, targetDate);
        for (JoySlot joySlot : joySlotList) {
            // 해당 시간대의 남아있는 자릿수 정보를 반환
            // 남아있는 자릿수가 0인 시간대는 예약 불가를 의미
            result.getRemaining_count_list().add(JoySlotTimeCountDto.timeCountOf(joySlot.getReservationTime(), info.getMaxCount() - joySlot.getCount()));
        }
        return result;
    }

}
