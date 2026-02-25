package com.example.monghyang.domain.qna.repository;

import com.example.monghyang.domain.qna.entity.QnaImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QnaImageRepository extends JpaRepository<QnaImage, Long> {

    List<QnaImage> findByQnaId(Long qnaId);

    void deleteByQnaId(Long qnaId);
}
