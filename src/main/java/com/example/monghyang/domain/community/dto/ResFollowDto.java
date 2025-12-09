package com.example.monghyang.domain.community.dto;

import com.example.monghyang.domain.community.entity.Follow;
import com.example.monghyang.domain.users.entity.Users;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ResFollowDto {
    private Long userId;
    private String nickname;
    private String email;
    private LocalDateTime followedAt;
    private Boolean isFollowing;  // 현재 로그인한 사용자가 이 유저를 팔로우하는지 여부

    // 팔로워 목록용 (나를 팔로우하는 사람)
    public static ResFollowDto fromFollower(Follow follow, Boolean isFollowing) {
        Users follower = follow.getFollower();
        return ResFollowDto.builder()
                .userId(follower.getId())
                .nickname(follower.getNickname())
                .email(follower.getEmail())
                .followedAt(follow.getCreatedAt())
                .isFollowing(isFollowing)
                .build();
    }

    // 팔로잉 목록용 (내가 팔로우하는 사람)
    public static ResFollowDto fromFollowing(Follow follow) {
        Users following = follow.getFollowing();
        return ResFollowDto.builder()
                .userId(following.getId())
                .nickname(following.getNickname())
                .email(following.getEmail())
                .followedAt(follow.getCreatedAt())
                .isFollowing(true)  // 팔로잉 목록이므로 항상 true
                .build();
    }
}
