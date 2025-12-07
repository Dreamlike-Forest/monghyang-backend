package com.example.monghyang.domain.joy.review.service;

import com.example.monghyang.domain.global.advice.ApplicationError;
import com.example.monghyang.domain.global.advice.ApplicationException;
import com.example.monghyang.domain.joy.entity.Joy;
import com.example.monghyang.domain.joy.entity.JoyOrder;
import com.example.monghyang.domain.joy.entity.JoyPaymentStatus;
import com.example.monghyang.domain.joy.repository.JoyOrderRepository;
import com.example.monghyang.domain.joy.repository.JoyRepository;
import com.example.monghyang.domain.joy.review.dto.ReqJoyReviewDto;
import com.example.monghyang.domain.joy.review.dto.ReqUpdateJoyReviewDto;
import com.example.monghyang.domain.joy.review.dto.ResJoyReviewDto;
import com.example.monghyang.domain.joy.review.entity.JoyReview;
import com.example.monghyang.domain.joy.review.repository.JoyReviewLikeHistoryRepository;
import com.example.monghyang.domain.joy.review.repository.JoyReviewRepository;
import com.example.monghyang.domain.users.entity.Users;
import com.example.monghyang.domain.users.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class JoyReviewService {
    public static final int JOY_REVIEW_PAGE_SIZE = 10;
    private final JoyReviewRepository joyReviewRepository;
    private final JoyRepository joyRepository;
    private final UsersRepository usersRepository;
    private final JoyReviewLikeHistoryRepository joyReviewLikeHistoryRepository;
    private final JoyOrderRepository joyOrderRepository;

    /**
     * 별점 값의 유효성 검증
     * @param star 검증할 별점
     */
    private void checkStarValid(Double star) {
        if(star < 0.0 || star > 5.0 || star % 0.5 != 0.0) {
            throw new ApplicationException(ApplicationError.JOY_REVIEW_STAR_INVALID);
        }
    }
    /**
     * 체험의 댓글형 리뷰 작성
     * @param userId 회원 식별자
     * @param dto ReqJoyReviewDto
     */
    public void addReview(Long userId, ReqJoyReviewDto dto) {
        checkStarValid(dto.getStar());
        Users users = usersRepository.findById(userId).orElseThrow(() ->
                new ApplicationException(ApplicationError.USER_NOT_FOUND));
        Joy joy = joyRepository.findById(dto.getJoy_id()).orElseThrow(() ->
                new ApplicationException(ApplicationError.JOY_NOT_FOUND));
        JoyOrder joyOrder = joyOrderRepository.findByIdAndUserId(dto.getJoy_order_id(), userId).orElseThrow(() ->
                // 해당 체험 이용 내역이 없으면 리뷰 작성 불가
                new ApplicationException(ApplicationError.JOY_REVIEW_CREATE_UNQUALIFIED));
        if(!joyOrder.getJoyPaymentStatus().equals(JoyPaymentStatus.PAID) || LocalDateTime.now().isBefore(joyOrder.getReservation())) {
            // 체험 예약이 '결제 완료'상태가 아니거나, '체험 시작 시각' 이전인 경우 리뷰 작성 불가
            throw new ApplicationException(ApplicationError.JOY_REVIEW_CREATE_UNQUALIFIED);
        }

        JoyReview joyReview = JoyReview.builder()
                .user(users).joy(joy).content(dto.getContent())
                .star(dto.getStar()).build();
        joyReviewRepository.save(joyReview);
    }

    /**
     * 체험의 댓글형 리뷰 수정
     * @param userId 회원 식별자
     * @param joyReviewId 리뷰 식별자
     * @param dto ReqJoyReviewDto의 content, star 필드만 사용
     */
    public void updateReview(Long userId, Long joyReviewId, ReqUpdateJoyReviewDto dto) {
        JoyReview joyReview = joyReviewRepository.findById(joyReviewId).orElseThrow(() ->
                new ApplicationException(ApplicationError.JOY_REVIEW_NOT_FOUND));
        if(!joyReview.getUser().getId().equals(userId)) {
            throw new ApplicationException(ApplicationError.FORBIDDEN);
        }

        if(dto.getContent() != null) {
            joyReview.updateContent(dto.getContent());
        }
        if(dto.getStar() != null) {
            checkStarValid(dto.getStar());
            joyReview.updateStar(dto.getStar());
        }
        joyReviewRepository.save(joyReview);
    }

    /**
     * 자신이 작성한 체험의 댓글형 리뷰 삭제(soft-delete)
     * @param userId 회원 식별자
     * @param joyReviewId 체험 식별자
     */
    public void deleteReview(Long userId, Long joyReviewId) {
        JoyReview joyReview = joyReviewRepository.findById(joyReviewId).orElseThrow(() ->
                new ApplicationException(ApplicationError.JOY_REVIEW_NOT_FOUND));
        if(!joyReview.getUser().getId().equals(userId)) {
            throw new ApplicationException(ApplicationError.FORBIDDEN);
        }
        joyReview.setDeleted(); // soft delete
        joyReviewRepository.save(joyReview);
    }

    /// 좋아요 추가/삭제 로직 추후 개선안
    /// 예상 병목 현상: 특정 유저가 좋아요 추가/삭제를 연타하는 경우, 잦은 레코드 삽입/삭제로 인해 인덱스 구조가 자주 변경되어 부하 유발
    /// 인덱스: (user_id, joy_review_id)
    /// 개선안
    ///     1. joy_review_like_history 테이블에 '좋아요 여부' 컬럼 추가
    ///     2. 좋아요 추가/삭제 마다 insert/delete을 수행하는 것이 아니라, '좋아요 여부'값만 수정
    ///     3. 좋아요 추가 로직: update 좋아요 여부 = 1 -> update된 레코드 수가 0이면 insert (특정 리뷰에 좋아요를 처음 누르는 경우)
    ///     4. 좋아요 삭제 로직: update 좋아요 여부 = 0
    /// 개선 효과: 잦은(혹은 악의적인) 좋아요 추가/삭제 시 인덱스 부하 방지
    /// 단점: '첫 좋아요' 시 update, insert 쿼리를 한번씩 실행해야 한다.

    /**
     * 특정 체험 댓글형 리뷰에 대해 좋아요 추가
     * @param userId 회원 식별자
     * @param joyReviewId 체험 댓글형 리뷰 식별자
     */
    @Transactional
    public void likeJoyReview(Long userId, Long joyReviewId) {
        try{
            joyReviewLikeHistoryRepository.insertByUserIdAndJoyReviewId(userId, joyReviewId);
        } catch (DataIntegrityViolationException e) {
            throw new ApplicationException(ApplicationError.JOY_REVIEW_LIKE_ADD_ERROR, e);
        }
        int increaseCnt = joyReviewRepository.increaseLike(joyReviewId);
        if(increaseCnt != 1) {
            throw new ApplicationException(ApplicationError.JOY_REVIEW_LIKE_ADD_ERROR);
        }
    }

    /**
     * 특정 체험 댓글형 리뷰에 대해 좋아요 추가
     * @param userId 회원 식별자
     * @param joyReviewId 체험 댓글형 리뷰 식별자
     */
    @Transactional
    public void unLikeJoyReview(Long userId, Long joyReviewId) {
        int joyReviewLikeHistoryCnt = joyReviewLikeHistoryRepository.deleteByUserIdAndJoyReviewId(userId, joyReviewId);
        if(joyReviewLikeHistoryCnt != 1) {
            throw new ApplicationException(ApplicationError.JOY_REVIEW_LIKE_CANCEL_ERROR);
        }
        int decreaseCnt = joyReviewRepository.decreaseLike(joyReviewId);
        if(decreaseCnt != 1) {
            throw new ApplicationException(ApplicationError.JOY_REVIEW_LIKE_CANCEL_ERROR);
        }
    }

    /**
     * 체험 구분 없이 최신순 조회
     * @param breweryId 양조장 식별자
     * @param startOffset 조회 시작 페이지 번호
     * @return
     */
    public Page<ResJoyReviewDto> findLatest(Long breweryId, Integer startOffset) {
        if(startOffset < 0) {
            startOffset = 0;
        }
        Pageable pageable = PageRequest.of(startOffset, JOY_REVIEW_PAGE_SIZE);
        Page<JoyReview> joyReviews = joyReviewRepository.findLatestByBrewery(pageable, breweryId);
        if(joyReviews.isEmpty()) {
            throw new ApplicationException(ApplicationError.JOY_REVIEW_NOT_FOUND);
        }
        return joyReviews.map(ResJoyReviewDto::userAndJoyJoinedJoyReviewFrom);
    }

    /**
     * 체험 구분 없이 좋아요순 조회
     * @param breweryId 양조장 식별자
     * @param startOffset 조회 페이지 번호
     * @return
     */
    public Page<ResJoyReviewDto> findLikesDesc(Long breweryId, Integer startOffset) {
        if(startOffset < 0) {
            startOffset = 0;
        }
        Pageable pageable = PageRequest.of(startOffset, JOY_REVIEW_PAGE_SIZE);
        Page<JoyReview> joyReviews = joyReviewRepository.findLikesDescByBrewery(pageable, breweryId);
        if(joyReviews.isEmpty()) {
            throw new ApplicationException(ApplicationError.JOY_REVIEW_NOT_FOUND);
        }
        return joyReviews.map(ResJoyReviewDto::userAndJoyJoinedJoyReviewFrom);
    }

    /**
     * 체험 구분 없이 별점순 조회
     * @param breweryId 양조장 식별자
     * @param startOffset 조회 페이지 번호
     * @return
     */
    public Page<ResJoyReviewDto> findStarDesc(Long breweryId, Integer startOffset) {
        if(startOffset < 0) {
            startOffset = 0;
        }
        Pageable pageable = PageRequest.of(startOffset, JOY_REVIEW_PAGE_SIZE);
        Page<JoyReview> joyReviews = joyReviewRepository.findStarDescByBrewery(pageable, breweryId);
        if(joyReviews.isEmpty()) {
            throw new ApplicationException(ApplicationError.JOY_REVIEW_NOT_FOUND);
        }
        return joyReviews.map(ResJoyReviewDto::userAndJoyJoinedJoyReviewFrom);
    }

    /**
     * 특정 체험 리뷰 최신순 조회
     * @param joyId 체험 식별자
     * @param startOffset 조회 시작 페이지
     * @return
     */
    public Page<ResJoyReviewDto> findByJoyLatest(Long joyId, Integer startOffset) {
        if(startOffset < 0) {
            startOffset = 0;
        }
        Pageable pageable = PageRequest.of(startOffset, JOY_REVIEW_PAGE_SIZE);
        Page<JoyReview> joyReviews = joyReviewRepository.findLatestByJoy(pageable, joyId);
        if(joyReviews.isEmpty()) {
            throw new ApplicationException(ApplicationError.JOY_REVIEW_NOT_FOUND);
        }
        return joyReviews.map(ResJoyReviewDto::userAndJoyJoinedJoyReviewFrom);
    }

    /**
     * 특정 체험 리뷰 좋아요순 조회
     * @param joyId 체험 식별자
     * @param startOffset 조회 시작 페이지
     * @return
     */
    public Page<ResJoyReviewDto> findByJoyLikesDesc(Long joyId, Integer startOffset) {
        if(startOffset < 0) {
            startOffset = 0;
        }
        Pageable pageable = PageRequest.of(startOffset, JOY_REVIEW_PAGE_SIZE);
        Page<JoyReview> joyReviews = joyReviewRepository.findLikesDescByJoy(pageable, joyId);
        if(joyReviews.isEmpty()) {
            throw new ApplicationException(ApplicationError.JOY_REVIEW_NOT_FOUND);
        }
        return joyReviews.map(ResJoyReviewDto::userAndJoyJoinedJoyReviewFrom);
    }

    /**
     * 특정 체험 리뷰 별점순 조회
     * @param joyId 체험 식별자
     * @param startOffset 조회 시작 페이지
     * @return
     */
    public Page<ResJoyReviewDto> findByJoyStarDesc(Long joyId, Integer startOffset) {
        if(startOffset < 0) {
            startOffset = 0;
        }
        Pageable pageable = PageRequest.of(startOffset, JOY_REVIEW_PAGE_SIZE);
        Page<JoyReview> joyReviews = joyReviewRepository.findStarDescByJoy(pageable, joyId);
        if(joyReviews.isEmpty()) {
            throw new ApplicationException(ApplicationError.JOY_REVIEW_NOT_FOUND);
        }
        return joyReviews.map(ResJoyReviewDto::userAndJoyJoinedJoyReviewFrom);
    }
}
