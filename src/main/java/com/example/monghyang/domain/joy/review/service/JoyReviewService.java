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

    @Transactional
    public void likeJoyReview(Long userId, Long joyReviewId) {

    }

    @Transactional
    public void unLikeJoyReview(Long userId, Long joyReviewId) {

    }
}
