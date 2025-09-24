package com.example.monghyang.domain.joy.service;

import com.example.monghyang.domain.joy.dto.ReqJoyOrderDto;
import com.example.monghyang.domain.joy.dto.ReqOrderDto;
import com.example.monghyang.domain.joy.dto.ReqUpdateJoyOrderDto;
import com.example.monghyang.domain.joy.dto.ResJoyOrderDto;
import com.example.monghyang.domain.joy.entity.Joy;
import com.example.monghyang.domain.joy.entity.JoyOrder;
import com.example.monghyang.domain.joy.entity.JoySlot;
import com.example.monghyang.domain.joy.repository.JoyOrderRepository;
import com.example.monghyang.domain.joy.repository.JoyRepository;
import com.example.monghyang.domain.joy.repository.JoySlotRepository;
import com.example.monghyang.domain.brewery.dto.OpeningHourDto;
import com.example.monghyang.domain.brewery.entity.Brewery;
import com.example.monghyang.domain.brewery.repository.BreweryRepository;
import com.example.monghyang.domain.global.advice.ApplicationError;
import com.example.monghyang.domain.global.advice.ApplicationException;
import com.example.monghyang.domain.users.entity.Users;
import com.example.monghyang.domain.users.repository.UsersRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@Slf4j
public class JoyOrderService {
    private static final int JOY_ORDER_PAGE_SIZE = 12;
    private final JoyOrderRepository joyOrderRepository;
    private final UsersRepository usersRepository;
    private final JoyRepository joyRepository;
    private final JoySlotRepository joySlotRepository;
    private final BreweryRepository breweryRepository;

    @Autowired
    public JoyOrderService(JoyOrderRepository joyOrderRepository, UsersRepository usersRepository, JoyRepository joyRepository, JoySlotRepository joySlotRepository, BreweryRepository breweryRepository) {
        this.joyOrderRepository = joyOrderRepository;
        this.usersRepository = usersRepository;
        this.joyRepository = joyRepository;
        this.joySlotRepository = joySlotRepository;
        this.breweryRepository = breweryRepository;
    }



    /**
     * 체험 시간대 유효성 검증
     * @param joyId 체험 식별자
     * @param reservation 예약 희망 시간대(혹은 수정 희망 시간대)
     * @param timeUnit 해당 체험의 체험 시간 단위
     */
    private void verifyReservationTime(Long joyId, LocalDateTime reservation, Integer timeUnit) {
        OpeningHourDto openingHourDto = breweryRepository.findOpeningHourByJoyId(joyId).orElseThrow(() ->
                new ApplicationException(ApplicationError.BREWERY_NOT_FOUND));
        long minDiff = ChronoUnit.MINUTES.between(openingHourDto.startTime(), reservation.toLocalTime());

        // 주문 요청의 체험 시작 시간이 양조장의 운영시간 내에 있는지 검증
        if(reservation.isBefore(LocalDateTime.now())
                || reservation.toLocalTime().isBefore(openingHourDto.startTime())
                || !reservation.toLocalTime().isBefore(openingHourDto.endTime())
                // 체험 시작 시간이 양조장의 '체험 시간 단위' 간격에 일치하는지 검증
                || minDiff % timeUnit != 0) {
            throw new ApplicationException(ApplicationError.JOY_ORDER_TIME_INVALID);
        }
    }

    // 체험 예약 요청 -> 본 서버에서 발급한 pgOrderId(UUID) 발급하여 반환
    public UUID prepareOrder(Long userId, ReqJoyOrderDto reqJoyOrderDto) {
        Users user = usersRepository.findById(userId).orElseThrow(() ->
                new ApplicationException(ApplicationError.USER_NOT_FOUND));
        Joy joy = joyRepository.findById(reqJoyOrderDto.getId()).orElseThrow(() ->
                new ApplicationException(ApplicationError.JOY_NOT_FOUND));

        verifyReservationTime(joy.getId(), reqJoyOrderDto.getReservation(), joy.getTimeUnit());

        UUID pgOrderId = UUID.randomUUID();
        JoyOrder joyOrder = JoyOrder.builder()
                .users(user).joy(joy).count(reqJoyOrderDto.getCount())
                .pgOrderId(pgOrderId).payerName(reqJoyOrderDto.getPayer_name())
                .payerPhone(reqJoyOrderDto.getPayer_phone()).reservation(reqJoyOrderDto.getReservation()).build();
        try{
            joySlotRepository.save(JoySlot.joyReservationOf(joy, reqJoyOrderDto.getReservation())); // uk 검증
            joyOrderRepository.save(joyOrder);
        } catch (DataIntegrityViolationException e) {
            throw new ApplicationException(ApplicationError.JOY_TIME_DUPLICATE);
        }
        return pgOrderId;
    }

    // 클라이언트에게 pgPaymentKey와 가격 정보를 받아 PG사로 결제 요청
    @Transactional
    public void requestOrderToPG(Long userId, ReqOrderDto reqOrderDto) {
        JoyOrder joyOrder = joyOrderRepository.findByPgOrderId(reqOrderDto.getPg_order_id()).orElseThrow(() ->
                new ApplicationException(ApplicationError.JOY_ORDER_NOT_FOUND));
        if(!joyOrder.getUsers().getId().equals(userId)) {
            throw new ApplicationException(ApplicationError.REQUEST_FORBIDDEN);
        }
        if(!joyOrder.getTotalPrice().equals(reqOrderDto.getTotal_price())) {
            // 클라이언트에게 받은 가격 정보와 DB의 '총 결제 금액'이 일치하는지 검증
            throw new ApplicationException(ApplicationError.MANIPULATE_ORDER_TOTAL_PRICE);
        }
        joyOrder.setPgPaymentKey(reqOrderDto.getPg_payment_key());

        /*

         PG사로 실제 결제를 요청하고 응답 결과를 처리하는 로직(외부 api 호출)
         => 별도의 메소드로 분리해서 컨트롤러에서 각각 따로따로 호출하도록 할 필요가 있어보임(DB 커넥션 과점유 방지)

         */
    }

    // 체험 시간 변경 요청(예약 전날까지만 가능)
    public void changeTime(Long userId, ReqUpdateJoyOrderDto reqUpdateJoyOrderDto) {
        JoyOrder joyOrder = joyOrderRepository.findById(reqUpdateJoyOrderDto.getId()).orElseThrow(() ->
                new ApplicationException(ApplicationError.JOY_ORDER_NOT_FOUND));
        if(!joyOrder.getUsers().getId().equals(userId)) {
            throw new ApplicationException(ApplicationError.REQUEST_FORBIDDEN);
        }
        if(reqUpdateJoyOrderDto.getReservation() == null) {
            return;
        }
        LocalDate now = LocalDate.now();
        if(ChronoUnit.DAYS.between(now, joyOrder.getReservation().toLocalDate()) < 1) {
            // 체험 일자 하루 전날까지만 시간대 변경 가능
            throw new ApplicationException(ApplicationError.JOY_ORDER_TIME_UPDATE_ERROR);
        }
        Integer timeUnit = joyRepository.findTimeUnitbyId(joyOrder.getJoy().getId()).orElseThrow(() ->
                new ApplicationException(ApplicationError.JOY_NOT_FOUND)); // 체험의 시간 단위 조회

        verifyReservationTime(joyOrder.getJoy().getId(), reqUpdateJoyOrderDto.getReservation(), timeUnit);

        // 체험 예약 슬롯 조회
        JoySlot slot = joySlotRepository.findByJoyIdAndReservation(joyOrder.getJoy().getId(), joyOrder.getReservation()).orElseThrow(() ->
                new ApplicationException(ApplicationError.JOY_ORDER_NOT_FOUND));
        slot.updateReservation(reqUpdateJoyOrderDto.getReservation());
        joyOrder.updateReservation(reqUpdateJoyOrderDto.getReservation());
        try{
            joySlotRepository.save(slot); // uk 검증
            joyOrderRepository.save(joyOrder);
        } catch (DataIntegrityViolationException e) {
            throw new ApplicationException(ApplicationError.JOY_TIME_DUPLICATE);
        }
    }

    // 체험 시간 변경(양조장용, 변경 조건 제한 없음)
    @Transactional
    public void changeTimeByBrewery(Long userId, ReqUpdateJoyOrderDto reqUpdateJoyOrderDto) {
        JoyOrder joyOrder = joyOrderRepository.findByIdAndBreweryUserId(reqUpdateJoyOrderDto.getId(), userId).orElseThrow(() ->
                new ApplicationException(ApplicationError.JOY_ORDER_NOT_FOUND));
        if(reqUpdateJoyOrderDto.getReservation() == null) {
            return;
        }
        Integer timeUnit = joyRepository.findTimeUnitbyId(joyOrder.getJoy().getId()).orElseThrow(() ->
                new ApplicationException(ApplicationError.JOY_NOT_FOUND)); // 체험의 시간 단위 조회

        verifyReservationTime(joyOrder.getJoy().getId(), reqUpdateJoyOrderDto.getReservation(), timeUnit);

        JoySlot slot = joySlotRepository.findByJoyIdAndReservation(joyOrder.getJoy().getId(), joyOrder.getReservation()).orElseThrow(() ->
                new ApplicationException(ApplicationError.JOY_ORDER_NOT_FOUND));
        slot.updateReservation(reqUpdateJoyOrderDto.getReservation());
        joyOrder.updateReservation(reqUpdateJoyOrderDto.getReservation());
        try{
            joySlotRepository.save(slot); // uk 검증
            joyOrderRepository.save(joyOrder);
        } catch (DataIntegrityViolationException e) {
            throw new ApplicationException(ApplicationError.JOY_TIME_DUPLICATE);
        }
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
        // 체험 예약 슬롯 제거
        JoySlot slot = joySlotRepository.findByJoyIdAndReservation(joyOrder.getJoy().getId(), joyOrder.getReservation()).orElseThrow(() ->
                new ApplicationException(ApplicationError.JOY_ORDER_NOT_FOUND));
        joySlotRepository.delete(slot);
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
        JoySlot slot = joySlotRepository.findByJoyIdAndReservation(joyOrder.getJoy().getId(), joyOrder.getReservation()).orElseThrow(() ->
                new ApplicationException(ApplicationError.JOY_ORDER_NOT_FOUND));
        joySlotRepository.delete(slot);
    }

    // 취소 수수료 로직 구현(예정)

    // 체험 예약 내역 삭제 처리(체험 종료 시간 이후에만 가능)
    @Transactional
    public void deleteHistory(Long userId, Long joyOrderId) {
        JoyOrder joyOrder = joyOrderRepository.findByIdAndUserId(joyOrderId, userId).orElseThrow(() ->
                new ApplicationException(ApplicationError.JOY_ORDER_NOT_FOUND));
        if(joyOrder.getIsCanceled() == true) {
            // 취소 처리된 예약 내역은 조건 없이 삭제 처리 가능
            joyOrder.setCanceled();
            return;
        }
        Integer timeUnit = joyRepository.findTimeUnitbyId(joyOrder.getJoy().getId()).orElseThrow(() ->
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

}
