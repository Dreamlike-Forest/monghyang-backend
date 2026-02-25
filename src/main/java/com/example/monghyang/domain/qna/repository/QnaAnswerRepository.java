package com.example.monghyang.domain.qna.repository;

import com.example.monghyang.domain.qna.entity.QnaAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QnaAnswerRepository extends JpaRepository<QnaAnswer, Long> {

    // 문의사항 ID로 답변 조회
    Optional<QnaAnswer> findByQnaId(Long qnaId);
}
