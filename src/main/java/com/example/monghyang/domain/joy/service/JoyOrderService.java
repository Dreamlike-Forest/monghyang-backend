package com.example.monghyang.domain.joy.service;

import com.example.monghyang.domain.batch.dto.JoyStatusHistoryBatchRow;
import com.example.monghyang.domain.batch.service.JoyOrderBatchService;
import com.example.monghyang.domain.batch.service.JoyOrderRefundService;
import com.example.monghyang.domain.brewery.dto.ReqClosedDateTimeDto;
import com.example.monghyang.domain.global.DayOfWeek;
import com.example.monghyang.domain.global.order.PaymentManager;
import com.example.monghyang.domain.global.pg.PayDBInfoDto;
import com.example.monghyang.domain.global.pg.Payment;
import com.example.monghyang.domain.joy.dto.ReqJoyPreOrderDto;
import com.example.monghyang.domain.global.order.ReqOrderDto;
import com.example.monghyang.domain.joy.dto.ReqUpdateJoyOrderDto;
import com.example.monghyang.domain.joy.dto.ResJoyOrderDto;
import com.example.monghyang.domain.joy.entity.*;
import com.example.monghyang.domain.joy.repository.JoyOrderRepository;
import com.example.monghyang.domain.joy.repository.JoyRepository;
import com.example.monghyang.domain.brewery.dto.JoyInfoDto;
import com.example.monghyang.domain.brewery.entity.Brewery;
import com.example.monghyang.domain.brewery.repository.BreweryRepository;
import com.example.monghyang.domain.brewery.repository.BreweryWeeklyBreakTimeRepository;
import com.example.monghyang.domain.global.advice.ApplicationError;
import com.example.monghyang.domain.global.advice.ApplicationException;
import com.example.monghyang.domain.joy.repository.JoyStatusHistoryRepository;
import com.example.monghyang.domain.joy.repository.JoyWeeklyStartTimeRepository;
import com.example.monghyang.domain.users.entity.Users;
import com.example.monghyang.domain.users.repository.UsersRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class JoyOrderService implements PaymentManager<ReqJoyPreOrderDto> {
    @Getter
    private static final int JOY_ORDER_PAGE_SIZE = 12;
    private final JoyOrderRepository joyOrderRepository;
    private final UsersRepository usersRepository;
    private final JoyRepository joyRepository;
    private final BreweryRepository breweryRepository;
    private final JoyStatusHistoryRepository joyStatusHistoryRepository;
    private final JoySlotService joySlotService;
    private final JoyOrderBatchService joyOrderBatchService;
    private final JoyOrderRefundService joyOrderRefundService;
    private final JoyWeeklyStartTimeRepository joyWeeklyStartTimeRepository;
    private final BreweryWeeklyBreakTimeRepository breweryWeeklyBreakTimeRepository;

    /**
     * 체험 시간대 유효성 검증
     * @param joyId 체험 식별자
     * @param joyInfoDto 체험 간단 정보 dto
     * @param reservationDate 예약 희망 일자
     * @param reservationTime 예약 희망 시간대
     * @param count 예약 인원 수                        
     */
    private void verifyReservation(Long joyId, JoyInfoDto joyInfoDto, LocalDate reservationDate, LocalTime reservationTime, Integer count) {
        if(count > joyInfoDto.maxCount()) {
            throw new ApplicationException(ApplicationError.JOY_COUNT_OVER);
        } else if(count < joyInfoDto.minCount()) {
            throw new ApplicationException(ApplicationError.JOY_COUNT_OVER);
        }
        long minDiff = ChronoUnit.MINUTES.between(joyInfoDto.breweryStartTime(), reservationTime);

        // 검증 과정
        // 1. 예약 일시가 현재보다 이전인지
        // 2. 예약 시간대가 영업 시작 시간보다 이전인지
        // 3. 예약의 체험 종료 시간이 영업 종료 시간대와 같거나 이후인지
        // 4. 체험 시작 시간이 양조장의 '체험 시간 단위' 간격에 일치하는지 검증
        if(LocalDateTime.of(reservationDate, reservationTime).isBefore(LocalDateTime.now())
                || reservationTime.isBefore(joyInfoDto.breweryStartTime())
                || reservationTime.plusMinutes(joyInfoDto.timeUnit()).isAfter(joyInfoDto.breweryEndTime())
                || minDiff % joyInfoDto.timeUnit() != 0) {
            throw new ApplicationException(ApplicationError.JOY_ORDER_TIME_INVALID);
        }

        DayOfWeek dayOfWeek = DayOfWeek.from(reservationDate.getDayOfWeek());
        verifyBreakTime(joyInfoDto.breweryId(), reservationDate, reservationTime, joyInfoDto.timeUnit(), dayOfWeek);

        // 예약일에 활성화된 체험 시작 시간 스냅샷 안에 요청 시간이 있는지 최종 검증한다.
        boolean existsStartTime = joyWeeklyStartTimeRepository.findActiveStartTimesByJoyIdAndDate(joyId, reservationDate, dayOfWeek)
                .stream()
                .anyMatch(startTime -> startTime.getStartTime().equals(reservationTime));
        if(!existsStartTime) {
            throw new ApplicationException(ApplicationError.JOY_ORDER_TIME_INVALID);
        }
    }

    /**
     * 체험 진행 시간이 양조장 휴게시간과 겹치면 예약 불가 예외를 발생시킵니다.
     *
     * @param breweryId       양조장 식별자
     * @param reservationDate 예약 대상일
     * @param reservationTime 예약 시작 시간
     * @param timeUnit        체험 진행 시간 단위
     * @param dayOfWeek       예약 대상일의 요일
     */
    private void verifyBreakTime(Long breweryId, LocalDate reservationDate, LocalTime reservationTime, Integer timeUnit, DayOfWeek dayOfWeek) {
        LocalTime reservationEndTime = reservationTime.plusMinutes(timeUnit);
        boolean overlapsBreakTime = breweryWeeklyBreakTimeRepository.findActiveBreakTimesByBreweryIdAndDate(breweryId, reservationDate, dayOfWeek)
                .stream()
                .anyMatch(b -> reservationTime.isBefore(b.getBreakEnd()) && reservationEndTime.isAfter(b.getBreakStart()));
        if (overlapsBreakTime) {
            throw new ApplicationException(ApplicationError.JOY_ORDER_TIME_INVALID);
        }
    }

    /**
     * joy slot 카운트 증가(예약 시). 1이 아닌 값이 반환되면 예약 실패를 의미. 프로세스 종료.
     * @param joyId
     * @param date
     * @param time
     * @param count
     */
    @Transactional
    public void reservationJoySlotCount(Long joyId, LocalDate date, LocalTime time, Integer count) {
        // 예약 일자의 요일로 변환하여 해당 날짜 기준 운영시간 스냅샷 조회
        DayOfWeek dayOfWeek = DayOfWeek.from(date.getDayOfWeek());
        JoyInfoDto joyInfoDto = breweryRepository.findJoyTimeInfoByJoyId(joyId, date, dayOfWeek).orElseThrow(() ->
                new ApplicationException(ApplicationError.BREWERY_NOT_FOUND));
        verifyReservation(joyId, joyInfoDto, date, time, count);
        // 체험 예약 슬롯 upsert(insert -> 중복 발생 시 조건부 update)
        joySlotService.reservationJoySlot(joyId, date, time, count, joyInfoDto.maxCount());
    }

    @Override
    @Transactional
    public UUID prepareOrder(Long userId, ReqJoyPreOrderDto dto) {
        try {
            Users user = usersRepository.findById(userId).orElseThrow(() ->
                    new ApplicationException(ApplicationError.USER_NOT_FOUND));
            Joy joy = joyRepository.findById(dto.getId()).orElseThrow(() ->
                    new ApplicationException(ApplicationError.JOY_NOT_FOUND));

            LocalDateTime reservationLocalDateTime = LocalDateTime.of(dto.getReservation_date(), dto.getReservation_time());
            UUID pgOrderId = UUID.randomUUID();
            JoyOrder joyOrder = JoyOrder.builder()
                    .users(user).joy(joy).count(dto.getCount())
                    .pgOrderId(pgOrderId).payerName(dto.getPayer_name())
                    .payerPhone(dto.getPayer_phone()).reservation(reservationLocalDateTime).build();
            try {
                joyOrderRepository.save(joyOrder);
                JoyStatusHistory history = JoyStatusHistory
                        .joyOrderToStatusReasonCodeOf(joyOrder, JoyPaymentStatus.PENDING, "pending");
                joyStatusHistoryRepository.save(history);
            } catch (DataIntegrityViolationException e) {
                throw new ApplicationException(ApplicationError.JOY_TIME_DUPLICATE);
            }
            return pgOrderId;
        } catch (ApplicationException e) {
            // pgOrderId 발급 중 문제가 발생하면 예약 슬롯 롤백
            joySlotService.decrementJoySlotCount(dto.getId(), dto.getReservation_date(), dto.getReservation_time(), dto.getCount());
            throw e;
        }
    }


    // 클라이언트에게 pgPaymentKey와 가격 정보를 받아 PG사로 결제 요청
    @Override
    @Transactional
    public void requestOrderToPG(Long userId, ReqOrderDto reqOrderDto) {
        JoyOrder joyOrder = joyOrderRepository.findByPgOrderId(reqOrderDto.getPg_order_id()).orElseThrow(() ->
                new ApplicationException(ApplicationError.JOY_ORDER_NOT_FOUND));
        if(!joyOrder.getUsers().getId().equals(userId)) {
            throw new ApplicationException(ApplicationError.REQUEST_FORBIDDEN);
        }
        if(!joyOrder.getTotalAmount().equals(reqOrderDto.getTotal_amount())) {
            // 클라이언트에게 받은 가격 정보와 DB의 '총 결제 금액'이 일치하는지 검증
            throw new ApplicationException(ApplicationError.MANIPULATE_ORDER_TOTAL_PRICE);
        }
        joyOrder.setPgPaymentKey(reqOrderDto.getPg_payment_key());

        /*

         PG사로 실제 결제를 요청하고 응답 결과를 처리하는 로직(외부 api 호출)
         => 별도의 메소드로 분리해서 컨트롤러에서 각각 따로따로 호출하도록 할 필요가 있어보임(DB 커넥션 과점유 방지)
         실패 시 history 테이블에도 저장, 예약 슬롯(JoySlot) 롤백

         */

        joyOrder.setPaid();
        JoyStatusHistory history = JoyStatusHistory
                .joyOrderToStatusReasonCodeOf(joyOrder, JoyPaymentStatus.PAID, "paid");
        joyStatusHistoryRepository.save(history);
    }

    @Override
    @Transactional
    public void setStatusFailed(UUID pgOrderId) {
        JoyOrder joyOrder = joyOrderRepository.findByPgOrderIdForSetFailed(pgOrderId).orElseThrow(() ->
                new ApplicationException(ApplicationError.JOY_ORDER_NOT_FOUND));
        joyOrder.setFailed();
        joyStatusHistoryRepository.save(JoyStatusHistory.joyOrderToStatusReasonCodeOf(joyOrder, JoyPaymentStatus.FAILED, "failed"));
        // 해당 시간대의 예약 가능 인원 수 롤백
        joySlotService.decrementJoySlotCount(joyOrder.getJoy().getId(), joyOrder.getReservation().toLocalDate(), joyOrder.getReservation().toLocalTime(), joyOrder.getCount());
    }


    /**
     * 예약 시간대 및 인원 변경 요청. 예약 전날까지 변경 요청 가능
     * @param userId
     * @param dto
     */
    @Transactional
    public void updateReservation(Long userId, ReqUpdateJoyOrderDto dto) {
        JoyOrder joyOrder = joyOrderRepository.findById(dto.getId()).orElseThrow(() ->
                new ApplicationException(ApplicationError.JOY_ORDER_NOT_FOUND));
        // 변경하려는 예약 일자의 요일로 변환하여 해당 날짜 기준 운영시간 스냅샷 조회
        DayOfWeek dayOfWeek = DayOfWeek.from(dto.getReservation_date().getDayOfWeek());
        JoyInfoDto joyInfoDto = breweryRepository.findJoyTimeInfoByJoyId(joyOrder.getJoy().getId(), dto.getReservation_date(), dayOfWeek).orElseThrow(() ->
                new ApplicationException(ApplicationError.BREWERY_NOT_FOUND));
        if(!joyOrder.getUsers().getId().equals(userId)) {
            throw new ApplicationException(ApplicationError.REQUEST_FORBIDDEN);
        }
        if(ChronoUnit.DAYS.between(LocalDate.now(), joyOrder.getReservation().toLocalDate()) < 1) {
            // 체험 일자 하루 전날까지만 시간대 변경 가능
            throw new ApplicationException(ApplicationError.JOY_ORDER_TIME_UPDATE_ERROR);
        }

        // 수정하려는 시간대 유효성 검증
        int count = (dto.getCount() == null) ? joyOrder.getCount() : dto.getCount();
        verifyReservation(joyOrder.getJoy().getId(), joyInfoDto, dto.getReservation_date(), dto.getReservation_time(), count);

        // 시간대 및 인원 변경 반영
        joySlotService.reservationJoySlot(joyOrder.getJoy().getId(), dto.getReservation_date(), dto.getReservation_time(), count, joyInfoDto.maxCount());

        // 기존 예약 슬롯 카운트 감소 or 제거
        joySlotService.decrementJoySlotCount(joyOrder.getJoy().getId(), joyOrder.getReservation().toLocalDate(), joyOrder.getReservation().toLocalTime(), joyOrder.getCount());

        // 예약 내역의 예약 시간대 정보 및 예약 인원수 갱신
        joyOrder.updateReservation(LocalDateTime.of(dto.getReservation_date(), dto.getReservation_time()));
        joyOrder.updateCount(count);
    }

    // 체험 시간 변경(양조장용, 변경 조건 제한 없음)
    @Transactional
    public void updateReservationByBrewery(Long userId, ReqUpdateJoyOrderDto dto) {
        JoyOrder joyOrder = joyOrderRepository.findByIdAndBreweryUserId(dto.getId(), userId).orElseThrow(() ->
                new ApplicationException(ApplicationError.JOY_ORDER_NOT_FOUND));
        // 변경하려는 예약 일자의 요일로 변환하여 해당 날짜 기준 운영시간 스냅샷 조회
        DayOfWeek dayOfWeek = DayOfWeek.from(dto.getReservation_date().getDayOfWeek());
        JoyInfoDto joyInfoDto = breweryRepository.findJoyTimeInfoByJoyId(joyOrder.getJoy().getId(), dto.getReservation_date(), dayOfWeek).orElseThrow(() ->
                new ApplicationException(ApplicationError.BREWERY_NOT_FOUND));
        // 수정하려는 시간대의 유효성 검증
        int count = (dto.getCount() == null) ? joyOrder.getCount() : dto.getCount();
        verifyReservation(joyOrder.getJoy().getId(), joyInfoDto, dto.getReservation_date(), dto.getReservation_time(), count);
        // 시간대 및 인원 변경 반영
        joySlotService.reservationJoySlot(joyOrder.getJoy().getId(), dto.getReservation_date(), dto.getReservation_time(), count, joyInfoDto.maxCount());
        // 기존 예약 슬롯 카운트 감소 or 제거
        joySlotService.decrementJoySlotCount(joyOrder.getJoy().getId(), joyOrder.getReservation().toLocalDate(), joyOrder.getReservation().toLocalTime(), joyOrder.getCount());

        // 예약 내역의 예약 시간대 정보 및 예약 인원수 갱신
        joyOrder.updateReservation(LocalDateTime.of(dto.getReservation_date(), dto.getReservation_time()));
        joyOrder.updateCount(count);
    }

    // 체험 취소(환불) 요청(체험 시작 하루 전까지만 취소 가능)
    @Transactional
    public void cancel(Long userId, Long joyOrderId) {
        JoyOrder joyOrder = joyOrderRepository.findByIdAndUserId(joyOrderId, userId).orElseThrow(() ->
                new ApplicationException(ApplicationError.JOY_ORDER_NOT_FOUND));
        LocalDate now = LocalDate.now();
        if(ChronoUnit.DAYS.between(now, joyOrder.getReservation().toLocalDate()) < 1) {
            // 체험 일자 하루 전날까지만 취소 가능
            throw new ApplicationException(ApplicationError.JOY_ORDER_CANCEL_ERROR);
        }

        /*
        PG사와 연동된 환불 로직
         */

        joyOrder.setCanceled();
        JoyStatusHistory history = JoyStatusHistory
                .joyOrderToStatusReasonCodeOf(joyOrder, JoyPaymentStatus.CANCELED, "canceled by user");
        joyStatusHistoryRepository.save(history);

        // 체험 예약 슬롯 카운트 값 롤백
        joySlotService.decrementJoySlotCount(joyOrder.getJoy().getId(), joyOrder.getReservation().toLocalDate(), joyOrder.getReservation().toLocalTime(), joyOrder.getCount());
    }

    // 관리자 권한 체험 취소 수행(조건 없음)
    @Transactional
    public void cancelByBrewery(Long userId, Long joyOrderId) {
        JoyOrder joyOrder = joyOrderRepository.findByIdAndUserId(joyOrderId, userId).orElseThrow(() ->
                new ApplicationException(ApplicationError.JOY_ORDER_NOT_FOUND));
        /*
        PG사와 연동된 환불 로직
         */
        joyOrder.setCanceled();

        JoyStatusHistory history = JoyStatusHistory
                .joyOrderToStatusReasonCodeOf(joyOrder, JoyPaymentStatus.CANCELED, "canceled by brewery");
        joyStatusHistoryRepository.save(history);

        // 체험 예약 슬롯 카운트 값 롤백
        joySlotService.decrementJoySlotCount(joyOrder.getJoy().getId(), joyOrder.getReservation().toLocalDate(), joyOrder.getReservation().toLocalTime(), joyOrder.getCount());
    }

    // 취소 수수료 로직 구현(예정)

    // 체험 예약 내역 삭제 처리(체험 종료 시간 이후에만 가능)
    @Transactional
    public void deleteHistory(Long userId, Long joyOrderId) {
        JoyOrder joyOrder = joyOrderRepository.findByIdAndUserId(joyOrderId, userId).orElseThrow(() ->
                new ApplicationException(ApplicationError.JOY_ORDER_NOT_FOUND));
        if(joyOrder.getJoyPaymentStatus() == JoyPaymentStatus.CANCELED) {
            // 취소 처리된 예약 내역은 조건 없이 삭제 처리 가능
            joyOrder.setCanceled();
            return;
        }
        Integer timeUnit = joyRepository.findTimeUnitByJoyId(joyOrder.getJoy().getId()).orElseThrow(() ->
                new ApplicationException(ApplicationError.JOY_NOT_FOUND)); // 체험의 시간 단위 조회
        LocalDateTime now = LocalDateTime.now();
        if(now.isBefore(joyOrder.getReservation().plusMinutes(timeUnit))) {
            throw new ApplicationException(ApplicationError.JOY_ORDER_DELETE_ERROR);
        }
        joyOrder.setDeleted();
    }

    // 특정 양조장의 체험 예약 내역 페이징 조회(최신순, 페이지 단위: 12)
    public Page<ResJoyOrderDto> getHistoryOfMyBrewery(Long userId, Integer startOffset) {
        Brewery brewery = breweryRepository.findByUserId(userId).orElseThrow(() ->
                new ApplicationException(ApplicationError.BREWERY_NOT_FOUND));
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt"); // 생성일자(예약 시점) 기준 내림차순
        Pageable pageable = PageRequest.of(startOffset, JOY_ORDER_PAGE_SIZE, sort);
        Page<ResJoyOrderDto> result = joyOrderRepository.findByBreweryId(brewery.getId(), pageable);
        if(result.isEmpty()) {
            throw new ApplicationException(ApplicationError.JOY_ORDER_NOT_FOUND);
        }
        return result;
    }

    // 특정 양조장의 특정 날짜의 체험 예약 현황을 조회
    public Page<ResJoyOrderDto> getHistoryOfMyBreweryByDate(Long userId, Integer startOffset, LocalDate date) {
        Brewery brewery = breweryRepository.findByUserId(userId).orElseThrow(() ->
                new ApplicationException(ApplicationError.BREWERY_NOT_FOUND));
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt"); // 생성일자(예약 시점) 기준 내림차순
        Pageable pageable = PageRequest.of(startOffset, JOY_ORDER_PAGE_SIZE, sort);
        Page<ResJoyOrderDto> result = joyOrderRepository.findByBreweryIdAndDate(brewery.getId(), pageable, date);
        if(result.isEmpty()) {
            throw new ApplicationException(ApplicationError.JOY_ORDER_NOT_FOUND);
        }
        return result;
    }

    // 자신의 예약 내역 조회
    public Page<ResJoyOrderDto> getHistoryOfUser(Long userId, Integer startOffset) {
        Users user = usersRepository.findById(userId).orElseThrow(() ->
                new ApplicationException(ApplicationError.USER_NOT_FOUND));
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt"); // 생성일자(예약 시점) 기준 내림차순
        Pageable pageable = PageRequest.of(startOffset, JOY_ORDER_PAGE_SIZE, sort);
        Page<ResJoyOrderDto> result = joyOrderRepository.findByUserId(user.getId(), pageable);
        if(result.isEmpty()) {
            throw new ApplicationException(ApplicationError.JOY_ORDER_NOT_FOUND);
        }
        return result;
    }

    /**
     * 해당되는 모든 'joy_order'의 상태를 'refund_requested'로 일괄 변경 및 상태 변경 로그 생성
     * @param breweryId 양조장 식별자
     * @param dto ReqClosedDateDto
     * @return
     */
    @Transactional
    public void setRefundRequestedJoyOrderByBreweryClosedDateAndTime(Long breweryId, ReqClosedDateTimeDto dto) {
        // 환불 대상 체험 예약을 모두 'refund_requested' 상태로 일괄 갱신
        List<Long> joyIdList = joyRepository.findIdByBreweryId(breweryId);
        if(joyIdList.isEmpty()) {
            return;
        }
        // 삭제 대상 '체험 예약 레코드' 선정
        List<Long> joyOrderList = joyOrderRepository.findIdByJoyIdListAndReservationAndStatus(joyIdList, dto.getClosed_date(), JoyPaymentStatus.PAID);
        // 체험 예약 레코드 상태를 refund_requested 로 일괄 갱신
        joyOrderRepository.updatePaymentStatusByJoyIdListAndStatus(joyOrderList, JoyPaymentStatus.REFUND_REQUESTED);

        // status log batch insert
        // 삭제 대상 '체험 예약 레코드 식별자'를 이용하여 JoyStatusHistoryBatchRow를 만든 다음 리스트화하여 인자로 전달
        int ret = joyOrderBatchService.batchInsert(joyOrderList.stream().map(jo -> {
            return new JoyStatusHistoryBatchRow(jo, JoyPaymentStatus.REFUND_REQUESTED, "양조장 일정 변경");
        }).toList());
        log.info("JoyStatusHistory Batch Insert Count: {}", ret); // batch insert count logging
    }

    /**
     * 양조장 운영시간/휴게시간 스케줄 변경으로 인해, effective_date 이후 예약된
     * 모든 해당 체험 예약을 REFUND_REQUESTED 상태로 일괄 변경합니다.
     * <p>
     * effective_date 당일 포함 이후에 예약된 PAID 상태의 체험 예약이 환불 처리 대상입니다.
     *
     * @param breweryId     양조장 식별자
     * @param effectiveDate 스케줄 적용 시작일 (이 날짜 포함 이후의 예약이 환불 처리 대상)
     */
    @Transactional
    public void setRefundRequestedByScheduleChange(Long breweryId, LocalDate effectiveDate) {
        // 해당 양조장의 체험 식별자 목록 조회
        List<Long> joyIdList = joyRepository.findIdByBreweryId(breweryId);
        if (joyIdList.isEmpty()) {
            return;
        }
        // effectiveDate 포함 이후 날짜에 예약된 PAID 상태의 체험 예약 식별자 목록 조회
        List<Long> joyOrderIdList = joyOrderRepository.findIdByJoyIdListAndReservationOnOrAfterAndStatus(
                joyIdList, effectiveDate, JoyPaymentStatus.PAID
        );
        if (joyOrderIdList.isEmpty()) {
            return;
        }
        // 조회된 예약 상태를 REFUND_REQUESTED로 일괄 갱신
        joyOrderRepository.updatePaymentStatusByJoyIdListAndStatus(joyOrderIdList, JoyPaymentStatus.REFUND_REQUESTED);
        // 상태 변경 이력 batch insert
        int ret = joyOrderBatchService.batchInsert(
                joyOrderIdList.stream()
                        .map(id -> new JoyStatusHistoryBatchRow(id, JoyPaymentStatus.REFUND_REQUESTED, "양조장 운영시간 변경"))
                        .toList()
        );
        log.info("스케줄 변경으로 인한 JoyStatusHistory Batch Insert 건수: {}", ret);
    }

    /**
     * 체험 시작 시간 스냅샷 변경으로 인해 적용일 이후 예약된 해당 체험 예약을 환불 요청 상태로 전환합니다.
     *
     * @param joyId 체험 식별자
     * @param effectiveDate 체험 일정 적용 시작일
     */
    @Transactional
    public void setRefundRequestedByJoyScheduleChange(Long joyId, LocalDate effectiveDate) {
        List<Long> joyOrderIdList = joyOrderRepository.findIdByJoyIdAndReservationFromAndPaymentStatusAndIsDeleted(
                joyId,
                effectiveDate.atStartOfDay(),
                JoyPaymentStatus.PAID,
                false
        );
        if(joyOrderIdList.isEmpty()) {
            return;
        }
        joyOrderRepository.updatePaymentStatusByJoyIdListAndStatus(joyOrderIdList, JoyPaymentStatus.REFUND_REQUESTED);
        int ret = joyOrderBatchService.batchInsert(
                joyOrderIdList.stream()
                        .map(id -> new JoyStatusHistoryBatchRow(id, JoyPaymentStatus.REFUND_REQUESTED, "체험 일정 변경"))
                        .toList()
        );
        log.info("체험 일정 변경으로 인한 JoyStatusHistory Batch Insert 건수: {}", ret);
    }

    /**
     * 체험 예약 환불 처리 스케줄러
     */
    @Scheduled(cron = "0 */5 * * * *")
    public void joyOrderRefundScheduling() {
        List<PayDBInfoDto> payDBInfoDtoList = joyOrderBatchService.getPayInfoListAndUpdateStatusToRefundProcessing();
        if(payDBInfoDtoList.isEmpty()) {
            System.out.println("환불 대상 레코드가 없습니다.");
            return;
        }
        List<Payment> refundResult = joyOrderRefundService.pgRefundProcess(payDBInfoDtoList);
        joyOrderBatchService.refundResultProcess(refundResult);
    }

}
