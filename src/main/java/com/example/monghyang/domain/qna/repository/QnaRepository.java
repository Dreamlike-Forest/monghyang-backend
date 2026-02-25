package com.example.monghyang.domain.qna.repository;

import com.example.monghyang.domain.qna.entity.Qna;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QnaRepository extends JpaRepository<Qna, Long> {

    // 사용자별 문의 목록 조회 (페이징)
    Page<Qna> findByUserIdAndIsDeletedFalseOrderByIdDesc(Long userId, Pageable pageable);

    // 전체 문의 목록 조회 - 관리자용 (페이징)
    Page<Qna> findByIsDeletedFalseOrderByIdDesc(Pageable pageable);

    // 단건 조회
    Optional<Qna> findByIdAndIsDeletedFalse(Long id);
}
