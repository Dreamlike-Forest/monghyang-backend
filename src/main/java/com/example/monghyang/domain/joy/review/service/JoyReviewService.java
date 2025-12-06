package com.example.monghyang.domain.joy.review.service;

import com.example.monghyang.domain.global.advice.ApplicationError;
import com.example.monghyang.domain.global.advice.ApplicationException;
import com.example.monghyang.domain.joy.entity.Joy;
import com.example.monghyang.domain.joy.repository.JoyRepository;
import com.example.monghyang.domain.joy.review.dto.ReqJoyReviewDto;
import com.example.monghyang.domain.joy.review.entity.JoyReview;
import com.example.monghyang.domain.joy.review.repository.JoyReviewRepository;
import com.example.monghyang.domain.users.entity.Users;
import com.example.monghyang.domain.users.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class JoyReviewService {
    private final JoyReviewRepository joyReviewRepository;
    private final JoyRepository joyRepository;
    private final UsersRepository usersRepository;

    /**
     * 체험의 댓글형 리뷰 작성
     * @param userId 회원 식별자
     * @param dto ReqJoyReviewDto
     */
    public void addReview(Long userId, ReqJoyReviewDto dto) {
        Users users = usersRepository.findById(userId).orElseThrow(() ->
                new ApplicationException(ApplicationError.USER_NOT_FOUND));
        Joy joy = joyRepository.findById(dto.getJoy_id()).orElseThrow(() ->
                new ApplicationException(ApplicationError.JOY_NOT_FOUND));

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
    public void updateReview(Long userId, Long joyReviewId, ReqJoyReviewDto dto) {
        JoyReview joyReview = joyReviewRepository.findById(joyReviewId).orElseThrow(() ->
                new ApplicationException(ApplicationError.JOY_REVIEW_NOT_FOUND));
        if(!joyReview.getUser().getId().equals(userId)) {
            throw new ApplicationException(ApplicationError.FORBIDDEN);
        }

        if(dto.getContent() != null) {
            joyReview.updateContent(dto.getContent());
        }
        if(dto.getStar() != null) {
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

    /**
     * 체험의 댓글형 리뷰의 조회수 1 증가
     * @param joyReviewId 리뷰 식별자
     * @throws ApplicationException 조회수 증가된 리뷰 레코드 수가 1이 아닌 경우 예외 발생
     */
    @Transactional
    public void increaseView(Long joyReviewId) {
        int ret = joyReviewRepository.increaseView(joyReviewId);
        if(ret != 1) {
            throw new ApplicationException(ApplicationError.JOY_REVIEW_VIEW_INCREASE_ERROR);
        }
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

    @Transactional
    public void likeJoyReview(Long userId, Long joyReviewId) {

    }

    @Transactional
    public void unLikeJoyReview(Long userId, Long joyReviewId) {

    }
}
