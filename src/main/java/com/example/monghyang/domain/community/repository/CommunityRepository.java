package com.example.monghyang.domain.community.repository;

import com.example.monghyang.domain.community.entity.Community;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommunityRepository extends JpaRepository<Community, Long> {
    List<Community> findByIsDeletedFalseOrderByCreatedAtDesc();

    List<Community> findByCategoryAndIsDeletedFalseOrderByCreatedAtDesc(String category);

    List<Community> findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(Long userId);

    Optional<Community> findByIdAndIsDeletedFalse(Long id);

    // 페이지네이션 메서드
    Page<Community> findByIsDeletedFalseOrderByCreatedAtDesc(Pageable pageable);

    Page<Community> findByCategoryAndIsDeletedFalseOrderByCreatedAtDesc(String category, Pageable pageable);

    Page<Community> findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(Long userId, Pageable pageable);
}
