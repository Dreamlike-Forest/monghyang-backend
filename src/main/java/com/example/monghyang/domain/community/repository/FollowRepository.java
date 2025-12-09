package com.example.monghyang.domain.community.repository;

import com.example.monghyang.domain.community.entity.Follow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {

    // 팔로우 관계 존재 여부 확인
    boolean existsByFollowerIdAndFollowingId(Long followerId, Long followingId);

    // 팔로우 관계 조회
    Optional<Follow> findByFollowerIdAndFollowingId(Long followerId, Long followingId);

    // 팔로워 목록 조회 (나를 팔로우하는 사람들)
    List<Follow> findByFollowingIdOrderByCreatedAtDesc(Long followingId);

    // 팔로워 목록 조회 (페이징)
    Page<Follow> findByFollowingIdOrderByCreatedAtDesc(Long followingId, Pageable pageable);

    // 팔로잉 목록 조회 (내가 팔로우하는 사람들)
    List<Follow> findByFollowerIdOrderByCreatedAtDesc(Long followerId);

    // 팔로잉 목록 조회 (페이징)
    Page<Follow> findByFollowerIdOrderByCreatedAtDesc(Long followerId, Pageable pageable);

    // 팔로워 수 조회
    long countByFollowingId(Long followingId);

    // 팔로잉 수 조회
    long countByFollowerId(Long followerId);
}
