package com.example.monghyang.domain.joy.review.service;

import com.example.monghyang.domain.global.advice.ApplicationError;
import com.example.monghyang.domain.global.advice.ApplicationException;
import com.example.monghyang.domain.joy.entity.Joy;
import com.example.monghyang.domain.joy.entity.JoyOrder;
import com.example.monghyang.domain.joy.entity.JoyPaymentStatus;
import com.example.monghyang.domain.joy.repository.JoyOrderRepository;
import com.example.monghyang.domain.joy.repository.JoyRepository;
import com.example.monghyang.domain.joy.review.dto.ReqJoyReviewDto;
import com.example.monghyang.domain.joy.review.repository.JoyReviewLikeHistoryRepository;
import com.example.monghyang.domain.joy.review.repository.JoyReviewRepository;
import com.example.monghyang.domain.users.entity.Users;
import com.example.monghyang.domain.users.repository.UsersRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class JoyReviewServiceTest {
    private static final ZoneId SEOUL_ZONE_ID = ZoneId.of("Asia/Seoul");
    private static final LocalDateTime FIXED_DATE_TIME = LocalDateTime.of(2026, 6, 1, 9, 30);
    private static final Clock FIXED_CLOCK = Clock.fixed(
            FIXED_DATE_TIME.atZone(SEOUL_ZONE_ID).toInstant(),
            SEOUL_ZONE_ID
    );

    @Mock
    JoyReviewRepository joyReviewRepository;
    @Mock
    JoyRepository joyRepository;
    @Mock
    UsersRepository usersRepository;
    @Mock
    JoyReviewLikeHistoryRepository joyReviewLikeHistoryRepository;
    @Mock
    JoyOrderRepository joyOrderRepository;

    JoyReviewService joyReviewService;

    @BeforeEach
    void setUp() {
        joyReviewService = new JoyReviewService(
                joyReviewRepository,
                joyRepository,
                usersRepository,
                joyReviewLikeHistoryRepository,
                joyOrderRepository,
                FIXED_CLOCK
        );
    }

    @Test
    @DisplayName("체험 시작 전이면 결제 완료 예약이어도 리뷰를 작성할 수 없다")
    void add_review_rejects_paid_order_before_reservation_time() {
        Long userId = 1L;
        ReqJoyReviewDto dto = reviewDto();
        JoyOrder joyOrder = mock(JoyOrder.class);
        given(usersRepository.findById(userId)).willReturn(Optional.of(mock(Users.class)));
        given(joyRepository.findById(dto.getJoy_id())).willReturn(Optional.of(mock(Joy.class)));
        given(joyOrderRepository.findByIdAndUserId(dto.getJoy_order_id(), userId)).willReturn(Optional.of(joyOrder));
        given(joyOrder.getJoyPaymentStatus()).willReturn(JoyPaymentStatus.PAID);
        given(joyOrder.getReservation()).willReturn(FIXED_DATE_TIME.plusMinutes(1));

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> joyReviewService.addReview(userId, dto)
        );

        assertEquals(ApplicationError.JOY_REVIEW_CREATE_UNQUALIFIED, exception.getApplicationError());
        verify(joyReviewRepository, never()).save(any());
    }

    @Test
    @DisplayName("체험 시작 이후 결제 완료 예약이면 리뷰를 저장한다")
    void add_review_saves_review_after_paid_reservation_time() {
        Long userId = 1L;
        ReqJoyReviewDto dto = reviewDto();
        JoyOrder joyOrder = mock(JoyOrder.class);
        given(usersRepository.findById(userId)).willReturn(Optional.of(mock(Users.class)));
        given(joyRepository.findById(dto.getJoy_id())).willReturn(Optional.of(mock(Joy.class)));
        given(joyOrderRepository.findByIdAndUserId(dto.getJoy_order_id(), userId)).willReturn(Optional.of(joyOrder));
        given(joyOrder.getJoyPaymentStatus()).willReturn(JoyPaymentStatus.PAID);
        given(joyOrder.getReservation()).willReturn(FIXED_DATE_TIME.minusMinutes(1));

        joyReviewService.addReview(userId, dto);

        verify(joyReviewRepository).save(any());
    }

    private ReqJoyReviewDto reviewDto() {
        ReqJoyReviewDto dto = new ReqJoyReviewDto();
        dto.setJoy_id(10L);
        dto.setJoy_order_id(99L);
        dto.setContent("체험이 좋았습니다.");
        dto.setStar(4.5);
        return dto;
    }
}
