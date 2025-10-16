package com.example.monghyang.domain.community.repository;

import com.example.monghyang.domain.community.entity.ImageCommunity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImageCommunityRepository extends JpaRepository<ImageCommunity, Long> {
    List<ImageCommunity> findByCommunityIdOrderByImageNumAsc(Long communityId);

    void deleteByCommunityId(Long communityId);
}
