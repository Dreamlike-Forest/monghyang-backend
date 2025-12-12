package com.example.monghyang.domain.community.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResFollowCountDto {
    private Long userId;
    private Long followerCount;   // 팔로워 수
    private Long followingCount;  // 팔로잉 수
    private Boolean isFollowing;  // 현재 로그인한 사용자가 이 유저를 팔로우하는지 여부

    public static ResFollowCountDto of(Long userId, Long followerCount, Long followingCount, Boolean isFollowing) {
        return ResFollowCountDto.builder()
                .userId(userId)
                .followerCount(followerCount)
                .followingCount(followingCount)
                .isFollowing(isFollowing)
                .build();
    }
}
