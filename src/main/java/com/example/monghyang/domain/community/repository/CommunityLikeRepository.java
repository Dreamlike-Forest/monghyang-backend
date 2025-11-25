package com.example.monghyang.domain.community.repository;

import com.example.monghyang.domain.community.entity.CommunityLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommunityLikeRepository extends JpaRepository<CommunityLike, Long> {
    Optional<CommunityLike> findByCommunityIdAndUserId(Long communityId, Long userId);

    boolean existsByCommunityIdAndUserId(Long communityId, Long userId);

    void deleteByCommunityIdAndUserId(Long communityId, Long userId);
}
