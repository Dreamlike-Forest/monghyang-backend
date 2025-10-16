package com.example.monghyang.domain.community.repository;

import com.example.monghyang.domain.community.entity.Community;
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
}
