package com.example.monghyang.mocktest;

import com.example.monghyang.domain.brewery.entity.Brewery;
import com.example.monghyang.domain.joy.dto.ReqJoyPreOrderDto;
import com.example.monghyang.domain.joy.dto.ReqOrderDto;
import com.example.monghyang.domain.joy.dto.ReqUpdateJoyOrderDto;
import com.example.monghyang.domain.joy.entity.Joy;
import com.example.monghyang.domain.joy.entity.JoyOrder;
import com.example.monghyang.domain.joy.entity.JoySlot;
import com.example.monghyang.domain.joy.repository.JoyOrderRepository;
import com.example.monghyang.domain.joy.repository.JoyRepository;
import com.example.monghyang.domain.joy.repository.JoySlotRepository;
import com.example.monghyang.domain.joy.service.JoyOrderService;
import com.example.monghyang.domain.brewery.dto.OpeningHourDto;
import com.example.monghyang.domain.brewery.repository.BreweryRepository;
import com.example.monghyang.domain.global.advice.ApplicationError;
import com.example.monghyang.domain.global.advice.ApplicationException;
import com.example.monghyang.domain.users.entity.Role;
import com.example.monghyang.domain.users.entity.Users;
import com.example.monghyang.domain.users.repository.UsersRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JoyOrderServiceTest {
    @Mock
    JoyOrderRepository joyOrderRepository;
    @Mock
    UsersRepository usersRepository;
    @Mock
    JoyRepository joyRepository;
    @Mock
    JoySlotRepository joySlotRepository;
    @Mock
    BreweryRepository breweryRepository;

    @InjectMocks
    JoyOrderService sut; // system under test

    // ===== Common Fixtures =====
    private Users user;
    private Joy joy;
    private LocalTime OPEN = LocalTime.of(9, 0);
    private LocalTime CLOSE = LocalTime.of(18, 0); // 종료 시각은 미포함 규칙

    @BeforeEach
    void setUp() {
        user = user();
        joy = joy(90, new BigDecimal("10000"));
        ReflectionTestUtils.setField(joy, "id", 1L); // ★ 중요: ID 강제 세팅
        ReflectionTestUtils.setField(user, "id", 1L); // ★ 중요: ID 강제 세팅
    }

    // ===== Helper builders =====
    private Users user() {
        Role role = mock(Role.class);
        return Users.generalBuilder()
                .role(role)
                .email("user@example.com")
                .password("pw")
                .nickname("nick")
                .name("name")
                .phone("010-0000-0000")
                .birth(LocalDate.of(2001,1,1))
                .gender(Boolean.TRUE)
                .address("addr")
                .address_detail("detail")
                .isAgreed(Boolean.TRUE)
                .build();
    }

    private Joy joy(int timeUnit, BigDecimal originPrice) {
        var brewery = Mockito.mock(Brewery.class);
        Joy j = Joy.joyBuilder()
                .brewery(brewery)
                .name("체험")
                .place("장소")
                .detail("상세")
                .originPrice(originPrice)
                .timeUnit(timeUnit)
                .imageKey("img")
                .build();
        // 강제로 ID 필드 세팅이 필요하면 리플렉션 사용 가능하지만, 본 테스트에서는 repository 응답만 사용하므로 생략
        return j;
    }

    private OpeningHourDto openingHour() {
        return new OpeningHourDto(OPEN, CLOSE);
    }

    // ============ prepareOrder ============
    @Nested
    class PrepareOrder {
        @Test
        @DisplayName("성공: 유효한 예약 요청이면 UUID 반환, JoySlot/Order 저장 호출")
        void success() {
            // given
            long userId = 10L;
            when(usersRepository.findById(userId)).thenReturn(Optional.of(user));
            when(joyRepository.findById(1L)).thenReturn(Optional.of(joy));
            when(breweryRepository.findOpeningHourByJoyId(1L)).thenReturn(Optional.of(openingHour()));

            ReqJoyPreOrderDto req = new ReqJoyPreOrderDto();
            req.setId(1L);
            req.setCount(2);
            req.setPayer_name("홍길동");
            req.setPayer_phone("010-1111-2222");
            // 지금으로부터 +2일 09:00 + (90분 * 2) = 12:00 예약
            LocalDateTime reservation = LocalDate.now().plusDays(2).atTime(12,0);
            req.setReservation(reservation);

            when(joySlotRepository.save(any(JoySlot.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(joyOrderRepository.save(any(JoyOrder.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // when
            UUID pgOrderId = sut.prepareOrder(userId, req);

            // then
            assertThat(pgOrderId).isNotNull();
            verify(joySlotRepository, times(1)).save(any(JoySlot.class));
            verify(joyOrderRepository, times(1)).save(any(JoyOrder.class));
        }

        @Test @DisplayName("실패: 사용자 없음 -> USER_NOT_FOUND")
        void userNotFound() {
            when(usersRepository.findById(10L)).thenReturn(Optional.empty());

            ReqJoyPreOrderDto req = new ReqJoyPreOrderDto();
            req.setId(1L);
            req.setCount(1);
            req.setPayer_name("n");
            req.setPayer_phone("p");
            req.setReservation(LocalDate.now().plusDays(1).atTime(10,0));

            assertThatThrownBy(() -> sut.prepareOrder(10L, req))
                    .isInstanceOfSatisfying(ApplicationException.class, ex ->
                            assertThat(ex.getApplicationError()).isEqualTo(ApplicationError.USER_NOT_FOUND));
        }

        @Test @DisplayName("실패: 체험 없음 -> JOY_NOT_FOUND")
        void joyNotFound() {
            when(usersRepository.findById(1L)).thenReturn(Optional.of(user));
            when(joyRepository.findById(1L)).thenReturn(Optional.empty());

            ReqJoyPreOrderDto req = new ReqJoyPreOrderDto();
            req.setId(1L);
            req.setCount(1);
            req.setPayer_name("n");
            req.setPayer_phone("p");
            req.setReservation(LocalDate.now().plusDays(1).atTime(10,0));

            assertThatThrownBy(() -> sut.prepareOrder(1L, req))
                    .isInstanceOfSatisfying(ApplicationException.class, ex ->
                            assertThat(ex.getApplicationError()).isEqualTo(ApplicationError.JOY_NOT_FOUND));
        }

        @Test @DisplayName("실패: 운영시간 정보 없음 -> BREWERY_NOT_FOUND")
        void openingHourNotFound() {
            when(usersRepository.findById(1L)).thenReturn(Optional.of(user));
            when(joyRepository.findById(1L)).thenReturn(Optional.of(joy));
            when(breweryRepository.findOpeningHourByJoyId(1L)).thenReturn(Optional.empty());

            ReqJoyPreOrderDto req = new ReqJoyPreOrderDto();
            req.setId(1L);
            req.setCount(1);
            req.setPayer_name("n");
            req.setPayer_phone("p");
            req.setReservation(LocalDate.now().plusDays(1).atTime(10,0));

            assertThatThrownBy(() -> sut.prepareOrder(1L, req))
                    .isInstanceOfSatisfying(ApplicationException.class, ex ->
                            assertThat(ex.getApplicationError()).isEqualTo(ApplicationError.BREWERY_NOT_FOUND));
        }

        @Test @DisplayName("실패: 예약 시간이 영업 외 혹은 간격 불일치 -> JOY_ORDER_TIME_INVALID")
        void invalidReservationTime() {
            when(usersRepository.findById(1L)).thenReturn(Optional.of(user));
            when(joyRepository.findById(1L)).thenReturn(Optional.of(joy));
            when(breweryRepository.findOpeningHourByJoyId(1L)).thenReturn(Optional.of(openingHour()));

            ReqJoyPreOrderDto req = new ReqJoyPreOrderDto();
            req.setId(1L);
            req.setCount(1);
            req.setPayer_name("n");
            req.setPayer_phone("p");
            // 10:30 은 90분 간격에 맞지 않음
            req.setReservation(LocalDate.now().plusDays(1).atTime(10, 0)); // ★ 90분 간격 위반

            assertThatThrownBy(() -> sut.prepareOrder(1L, req))
                    .isInstanceOfSatisfying(ApplicationException.class, ex ->
                            assertThat(ex.getApplicationError()).isEqualTo(ApplicationError.JOY_ORDER_TIME_INVALID));
        }

        @Test @DisplayName("실패: 슬롯 UK 충돌 -> JOY_TIME_DUPLICATE")
        void duplicateJoyTime() {
            when(usersRepository.findById(1L)).thenReturn(Optional.of(user));
            when(joyRepository.findById(1L)).thenReturn(Optional.of(joy));
            when(breweryRepository.findOpeningHourByJoyId(1L)).thenReturn(Optional.of(openingHour()));

            ReqJoyPreOrderDto req = new ReqJoyPreOrderDto();
            req.setId(1L);
            req.setCount(1);
            req.setPayer_name("n");
            req.setPayer_phone("p");
            req.setReservation(LocalDate.now().plusDays(1).atTime(12,0));

            when(joySlotRepository.save(any(JoySlot.class)))
                    .thenThrow(new DataIntegrityViolationException("duplicate"));

            assertThatThrownBy(() -> sut.prepareOrder(1L, req))
                    .isInstanceOfSatisfying(ApplicationException.class, ex ->
                            assertThat(ex.getApplicationError()).isEqualTo(ApplicationError.JOY_TIME_DUPLICATE));
        }
    }

    // ============ requestOrderToPG ============
    @Nested
    class RequestOrderToPG {
        @Test @DisplayName("성공: 총액 일치 -> pgPaymentKey 설정")
        void success() {
            UUID pgOrderId = UUID.randomUUID();
            JoyOrder order = new JoyOrder(joy, user, 2, pgOrderId, "홍길동", "010", LocalDate.now().plusDays(3).atTime(12,0));
            when(joyOrderRepository.findByPgOrderId(pgOrderId)).thenReturn(Optional.of(order));

            ReqOrderDto req = new ReqOrderDto();
            req.setPg_order_id(pgOrderId);
            req.setPg_payment_key("pg-key");
            req.setTotal_price(order.getTotalPrice());

            // when
            sut.requestOrderToPG(user.getId(), req);

            // then
            assertThat(order.getPgPaymentKey()).isEqualTo("pg-key");
        }

        @Test @DisplayName("실패: 주문 미존재 -> JOY_ORDER_NOT_FOUND")
        void notFound() {
            UUID pgOrderId = UUID.randomUUID();
            when(joyOrderRepository.findByPgOrderId(pgOrderId)).thenReturn(Optional.empty());

            ReqOrderDto req = new ReqOrderDto();
            req.setPg_order_id(pgOrderId);
            req.setPg_payment_key("pg-key");
            req.setTotal_price(new BigDecimal("1000"));

            assertThatThrownBy(() -> sut.requestOrderToPG(user.getId(), req))
                    .isInstanceOfSatisfying(ApplicationException.class, ex ->
                            assertThat(ex.getApplicationError()).isEqualTo(ApplicationError.JOY_ORDER_NOT_FOUND));
        }

        @Test @DisplayName("실패: 총액 불일치 -> MANIPULATE_ORDER_TOTAL_PRICE")
        void priceMismatch() {
            UUID pgOrderId = UUID.randomUUID();
            JoyOrder order = new JoyOrder(joy, user, 1, pgOrderId, "홍길동", "010", LocalDate.now().plusDays(3).atTime(12,0));
            when(joyOrderRepository.findByPgOrderId(pgOrderId)).thenReturn(Optional.of(order));

            ReqOrderDto req = new ReqOrderDto();
            req.setPg_order_id(pgOrderId);
            req.setPg_payment_key("pg-key");
            req.setTotal_price(order.getTotalPrice().add(BigDecimal.ONE));

            assertThatThrownBy(() -> sut.requestOrderToPG(user.getId(), req))
                    .isInstanceOfSatisfying(ApplicationException.class, ex ->
                            assertThat(ex.getApplicationError()).isEqualTo(ApplicationError.MANIPULATE_ORDER_TOTAL_PRICE));
        }
    }

    // ============ changeTime (user) ============
    @Nested
    class ChangeTimeByUser {
        @Test @DisplayName("성공: 작성자 동일, D-1 충족, 유효한 새 시간 -> 슬롯/주문 모두 업데이트")
        void success() {
            LocalDateTime oldResv = LocalDate.now().plusDays(5).atTime(12,0);
            LocalDateTime newResv = LocalDate.now().plusDays(5).atTime(13,30); // 90분 간격 ok

            JoyOrder order = new JoyOrder(joy, user, 1, UUID.randomUUID(), "홍", "010", oldResv);
            when(joyOrderRepository.findById(100L)).thenReturn(Optional.of(order));
            when(joyRepository.findTimeUnitbyId(anyLong())).thenReturn(Optional.of(joy.getTimeUnit()));
            when(breweryRepository.findOpeningHourByJoyId(anyLong())).thenReturn(Optional.of(openingHour()));

            JoySlot slot = JoySlot.joyReservationOf(joy, oldResv);
            when(joySlotRepository.findByJoyIdAndReservation(anyLong(), eq(oldResv))).thenReturn(Optional.of(slot));
            when(joySlotRepository.save(any(JoySlot.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(joyOrderRepository.save(any(JoyOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

            ReqUpdateJoyOrderDto req = new ReqUpdateJoyOrderDto();
            req.setId(100L);
            req.setReservation(newResv);

            // when
            sut.changeTime(nullSafeId(user), req);

            // then
            assertThat(order.getReservation()).isEqualTo(newResv.withSecond(0).withNano(0));
            assertThat(slot.getReservation()).isEqualTo(newResv.withSecond(0).withNano(0));
        }

        @Test @DisplayName("실패: 작성자 불일치 -> REQUEST_FORBIDDEN")
        void forbidden() {
            LocalDateTime oldResv = LocalDate.now().plusDays(5).atTime(12,0);
            JoyOrder order = new JoyOrder(joy, user, 1, UUID.randomUUID(), "홍", "010", oldResv);
            when(joyOrderRepository.findById(100L)).thenReturn(Optional.of(order));

            ReqUpdateJoyOrderDto req = new ReqUpdateJoyOrderDto();
            req.setId(100L);
            req.setReservation(oldResv.plusMinutes(90));

            assertThatThrownBy(() -> sut.changeTime(999L, req))
                    .isInstanceOfSatisfying(ApplicationException.class, ex ->
                            assertThat(ex.getApplicationError()).isEqualTo(ApplicationError.REQUEST_FORBIDDEN));
        }

        @Test @DisplayName("실패: D-1 위반 -> JOY_ORDER_TIME_UPDATE_ERROR")
        void dMinus1Violation() {
            LocalDateTime oldResv = LocalDate.now().atTime(12, 0);
            JoyOrder order = new JoyOrder(joy, user, 1, UUID.randomUUID(), "홍", "010", oldResv);
            when(joyOrderRepository.findById(100L)).thenReturn(Optional.of(order));

            ReqUpdateJoyOrderDto req = new ReqUpdateJoyOrderDto();
            req.setId(100L);
            req.setReservation(oldResv.plusMinutes(90));

            assertThatThrownBy(() -> sut.changeTime(nullSafeId(user), req))
                    .isInstanceOfSatisfying(ApplicationException.class, ex ->
                            assertThat(ex.getApplicationError()).isEqualTo(ApplicationError.JOY_ORDER_TIME_UPDATE_ERROR));
        }

        @Test @DisplayName("실패: 시간 유효성 위반 -> JOY_ORDER_TIME_INVALID")
        void invalidNewTime() {
            LocalDateTime oldResv = LocalDate.now().plusDays(5).atTime(12, 0); // D-1 통과
            LocalDateTime newResv = LocalDate.now().plusDays(5).atTime(10, 0); // 90분 간격 X
            JoyOrder order = new JoyOrder(joy, user, 1, UUID.randomUUID(), "홍", "010", oldResv);

            when(joyOrderRepository.findById(100L)).thenReturn(Optional.of(order));
            when(joyRepository.findTimeUnitbyId(anyLong())).thenReturn(Optional.of(joy.getTimeUnit()));
            when(breweryRepository.findOpeningHourByJoyId(anyLong())).thenReturn(Optional.of(openingHour()));

            ReqUpdateJoyOrderDto req = new ReqUpdateJoyOrderDto();
            req.setId(100L);
            req.setReservation(newResv);

            assertThatThrownBy(() -> sut.changeTime(nullSafeId(user), req))
                    .isInstanceOfSatisfying(ApplicationException.class, ex ->
                            assertThat(ex.getApplicationError()).isEqualTo(ApplicationError.JOY_ORDER_TIME_INVALID));
        }

        @Test @DisplayName("실패: 저장 시 UK 충돌 -> JOY_TIME_DUPLICATE")
        void duplicateOnSave() {
            LocalDateTime oldResv = LocalDate.now().plusDays(5).atTime(12,0);
            LocalDateTime newResv = LocalDate.now().plusDays(5).atTime(13,30);
            JoyOrder order = new JoyOrder(joy, user, 1, UUID.randomUUID(), "홍", "010", oldResv);

            when(joyOrderRepository.findById(100L)).thenReturn(Optional.of(order));
            when(joyRepository.findTimeUnitbyId(anyLong())).thenReturn(Optional.of(joy.getTimeUnit()));
            when(breweryRepository.findOpeningHourByJoyId(anyLong())).thenReturn(Optional.of(openingHour()));

            JoySlot slot = JoySlot.joyReservationOf(joy, oldResv);
            when(joySlotRepository.findByJoyIdAndReservation(anyLong(), eq(oldResv))).thenReturn(Optional.of(slot));
            when(joySlotRepository.save(any(JoySlot.class)))
                    .thenThrow(new DataIntegrityViolationException("duplicate"));

            ReqUpdateJoyOrderDto req = new ReqUpdateJoyOrderDto();
            req.setId(100L);
            req.setReservation(newResv);

            assertThatThrownBy(() -> sut.changeTime(nullSafeId(user), req))
                    .isInstanceOfSatisfying(ApplicationException.class, ex ->
                            assertThat(ex.getApplicationError()).isEqualTo(ApplicationError.JOY_TIME_DUPLICATE));
        }
    }

    // ============ changeTimeByBrewery ============
    @Nested
    class ChangeTimeByBrewery {
        @Test @DisplayName("성공: 양조장 소유주가 시간 변경")
        void success() {
            long breweryUserId = 77L;
            LocalDateTime oldResv = LocalDate.now().plusDays(7).atTime(9,0);
            LocalDateTime newResv = LocalDate.now().plusDays(7).atTime(10,30);

            JoyOrder order = new JoyOrder(joy, user, 2, UUID.randomUUID(), "홍", "010", oldResv);
            when(joyOrderRepository.findByIdAndBreweryUserId(100L, breweryUserId))
                    .thenReturn(Optional.of(order));
            when(joyRepository.findTimeUnitbyId(anyLong())).thenReturn(Optional.of(joy.getTimeUnit()));
            when(breweryRepository.findOpeningHourByJoyId(anyLong())).thenReturn(Optional.of(openingHour()));

            JoySlot slot = JoySlot.joyReservationOf(joy, oldResv);
            when(joySlotRepository.findByJoyIdAndReservation(anyLong(), eq(oldResv))).thenReturn(Optional.of(slot));
            when(joySlotRepository.save(any(JoySlot.class))).thenAnswer(invocation -> invocation.getArgument(0));

            ReqUpdateJoyOrderDto req = new ReqUpdateJoyOrderDto();
            req.setId(100L);
            req.setReservation(newResv);

            sut.changeTimeByBrewery(breweryUserId, req);

            assertThat(order.getReservation()).isEqualTo(newResv.withSecond(0).withNano(0));
            assertThat(slot.getReservation()).isEqualTo(newResv.withSecond(0).withNano(0));
        }

        @Test @DisplayName("실패: 주문 미존재 -> JOY_ORDER_NOT_FOUND")
        void notFound() {
            when(joyOrderRepository.findByIdAndBreweryUserId(100L, 77L))
                    .thenReturn(Optional.empty());

            ReqUpdateJoyOrderDto req = new ReqUpdateJoyOrderDto();
            req.setId(100L);
            req.setReservation(LocalDate.now().plusDays(10).atTime(12,0));

            assertThatThrownBy(() -> sut.changeTimeByBrewery(77L, req))
                    .isInstanceOfSatisfying(ApplicationException.class, ex ->
                            assertThat(ex.getApplicationError()).isEqualTo(ApplicationError.JOY_ORDER_NOT_FOUND));
        }
    }

    // ============ cancel (user) ============
    @Nested
    class CancelByUser {
        @Test @DisplayName("성공: D-1 충족 -> 상태 취소 및 슬롯 삭제")
        void success() {
            LocalDateTime resv = LocalDate.now().plusDays(3).atTime(12,0);
            JoyOrder order = new JoyOrder(joy, user, 1, UUID.randomUUID(), "홍", "010", resv);
            when(joyOrderRepository.findByIdAndUserId(200L, nullSafeId(user))).thenReturn(Optional.of(order));
            JoySlot slot = JoySlot.joyReservationOf(joy, resv);
            when(joySlotRepository.findByJoyIdAndReservation(anyLong(), eq(resv))).thenReturn(Optional.of(slot));

            sut.cancel(nullSafeId(user), 200L);

            assertThat(order.getIsCanceled()).isTrue();
            verify(joySlotRepository).delete(slot);
        }

        @Test @DisplayName("실패: D-1 위반 -> JOY_ORDER_CANCEL_ERROR")
        void dMinus1Violation() {
            LocalDateTime resv = LocalDate.now().atTime(12, 0);
            JoyOrder order = new JoyOrder(joy, user, 1, UUID.randomUUID(), "홍", "010", resv);
            when(joyOrderRepository.findByIdAndUserId(200L, nullSafeId(user))).thenReturn(Optional.of(order));

            assertThatThrownBy(() -> sut.cancel(nullSafeId(user), 200L))
                    .isInstanceOfSatisfying(ApplicationException.class, ex ->
                    assertThat(ex.getApplicationError()).isEqualTo(ApplicationError.JOY_ORDER_CANCEL_ERROR));
        }
    }

    // ============ cancelByBrewery ============
    @Nested
    class CancelByBrewery {
        @Test @DisplayName("성공: 양조장 측 취소 -> 상태 취소 및 슬롯 삭제")
        void success() {
            LocalDateTime resv = LocalDate.now().plusDays(10).atTime(12,0);
            JoyOrder order = new JoyOrder(joy, user, 1, UUID.randomUUID(), "홍", "010", resv);
            when(joyOrderRepository.findByIdAndUserId(300L, nullSafeId(user))).thenReturn(Optional.of(order));
            JoySlot slot = JoySlot.joyReservationOf(joy, resv);
            when(joySlotRepository.findByJoyIdAndReservation(anyLong(), eq(resv))).thenReturn(Optional.of(slot));

            sut.cancelByBrewery(nullSafeId(user), 300L);

            assertThat(order.getIsCanceled()).isTrue();
            verify(joySlotRepository).delete(slot);
        }
    }

    // ============ deleteHistory ============
    @Nested
    class DeleteHistory {
        @Test @DisplayName("성공: 체험 종료 이후 -> 삭제 플래그 true")
        void success() {
            // timeUnit=90, 종료시각 = resv + 90분. 현재가 그 이후가 되도록 과거 예약 생성
            LocalDateTime resv = LocalDate.now().minusDays(1).atTime(10,0);
            JoyOrder order = new JoyOrder(joy, user, 1, UUID.randomUUID(), "홍", "010", resv);
            when(joyOrderRepository.findByIdAndUserId(400L, nullSafeId(user))).thenReturn(Optional.of(order));
            when(joyRepository.findTimeUnitbyId(anyLong())).thenReturn(Optional.of(joy.getTimeUnit()));

            sut.deleteHistory(nullSafeId(user), 400L);

            assertThat(order.getIsDeleted()).isTrue();
        }

        @Test @DisplayName("실패: 종료 전 -> JOY_ORDER_DELETE_ERROR")
        void notEndedYet() {
            LocalDateTime resv = LocalDate.now().plusDays(1).atTime(10,0);
            JoyOrder order = new JoyOrder(joy, user, 1, UUID.randomUUID(), "홍", "010", resv);
            when(joyOrderRepository.findByIdAndUserId(400L, nullSafeId(user))).thenReturn(Optional.of(order));
            when(joyRepository.findTimeUnitbyId(anyLong())).thenReturn(Optional.of(joy.getTimeUnit()));

            assertThatThrownBy(() -> sut.deleteHistory(nullSafeId(user), 400L))
                    .isInstanceOfSatisfying(ApplicationException.class, ex ->
                            assertThat(ex.getApplicationError()).isEqualTo(ApplicationError.JOY_ORDER_DELETE_ERROR));
        }
    }

    // ======= utility =======
    private Long nullSafeId(Users u) {
        // 테스트에서 Users.id 접근이 필요할 때, 엔티티의 @Id가 null이더라도 레포지토리 쿼리는 모킹으로 처리되므로 단순 고정값 사용
        return 1L;
    }
}
