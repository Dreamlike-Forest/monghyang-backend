package com.example.monghyang.domain.community.service;

import com.example.monghyang.domain.community.dto.PageResponseDto;
import com.example.monghyang.domain.community.dto.ResFollowCountDto;
import com.example.monghyang.domain.community.dto.ResFollowDto;
import com.example.monghyang.domain.community.entity.Follow;
import com.example.monghyang.domain.community.repository.FollowRepository;
import com.example.monghyang.domain.global.advice.ApplicationError;
import com.example.monghyang.domain.global.advice.ApplicationException;
import com.example.monghyang.domain.users.entity.Users;
import com.example.monghyang.domain.users.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FollowService {
    private final FollowRepository followRepository;
    private final UsersRepository usersRepository;

    private static final int PAGE_SIZE = 20;

    @Transactional
    public void follow(Long followerId, Long followingId) {
        // 자기 자신을 팔로우할 수 없음
        if (followerId.equals(followingId)) {
            throw new ApplicationException(ApplicationError.FORBIDDEN);
        }

        Users follower = usersRepository.findById(followerId)
                .orElseThrow(() -> new ApplicationException(ApplicationError.USER_NOT_FOUND));

        Users following = usersRepository.findById(followingId)
                .orElseThrow(() -> new ApplicationException(ApplicationError.USER_NOT_FOUND));

        // 이미 팔로우 중인지 확인
        if (followRepository.existsByFollowerIdAndFollowingId(followerId, followingId)) {
            throw new ApplicationException(ApplicationError.ALREADY_FOLLOWED);
        }

        Follow follow = Follow.builder()
                .follower(follower)
                .following(following)
                .build();

        followRepository.save(follow);
    }

    @Transactional
    public void unfollow(Long followerId, Long followingId) {
        Follow follow = followRepository.findByFollowerIdAndFollowingId(followerId, followingId)
                .orElseThrow(() -> new ApplicationException(ApplicationError.FOLLOW_NOT_FOUND));

        followRepository.delete(follow);
    }

    // 팔로워 목록 조회 (나를 팔로우하는 사람들)
    public List<ResFollowDto> getFollowers(Long userId, Long currentUserId) {
        List<Follow> followers = followRepository.findByFollowingIdOrderByCreatedAtDesc(userId);

        return followers.stream()
                .map(follow -> {
                    Boolean isFollowing = currentUserId != null
                            ? followRepository.existsByFollowerIdAndFollowingId(currentUserId, follow.getFollower().getId())
                            : false;
                    return ResFollowDto.fromFollower(follow, isFollowing);
                })
                .collect(Collectors.toList());
    }

    // 팔로워 목록 조회 (페이징)
    public PageResponseDto<ResFollowDto> getFollowersWithPaging(Long userId, Long currentUserId, int page) {
        Pageable pageable = PageRequest.of(page, PAGE_SIZE);
        Page<Follow> followerPage = followRepository.findByFollowingIdOrderByCreatedAtDesc(userId, pageable);

        Page<ResFollowDto> dtoPage = followerPage.map(follow -> {
            Boolean isFollowing = currentUserId != null
                    ? followRepository.existsByFollowerIdAndFollowingId(currentUserId, follow.getFollower().getId())
                    : false;
            return ResFollowDto.fromFollower(follow, isFollowing);
        });

        return PageResponseDto.from(dtoPage);
    }

    // 팔로잉 목록 조회 (내가 팔로우하는 사람들)
    public List<ResFollowDto> getFollowings(Long userId) {
        List<Follow> followings = followRepository.findByFollowerIdOrderByCreatedAtDesc(userId);

        return followings.stream()
                .map(ResFollowDto::fromFollowing)
                .collect(Collectors.toList());
    }

    // 팔로잉 목록 조회 (페이징)
    public PageResponseDto<ResFollowDto> getFollowingsWithPaging(Long userId, int page) {
        Pageable pageable = PageRequest.of(page, PAGE_SIZE);
        Page<Follow> followingPage = followRepository.findByFollowerIdOrderByCreatedAtDesc(userId, pageable);

        Page<ResFollowDto> dtoPage = followingPage.map(ResFollowDto::fromFollowing);

        return PageResponseDto.from(dtoPage);
    }

    // 팔로우 상태 및 카운트 조회
    public ResFollowCountDto getFollowCount(Long userId, Long currentUserId) {
        long followerCount = followRepository.countByFollowingId(userId);
        long followingCount = followRepository.countByFollowerId(userId);

        Boolean isFollowing = currentUserId != null
                ? followRepository.existsByFollowerIdAndFollowingId(currentUserId, userId)
                : false;

        return ResFollowCountDto.of(userId, followerCount, followingCount, isFollowing);
    }

    // 팔로우 여부 확인
    public boolean isFollowing(Long followerId, Long followingId) {
        return followRepository.existsByFollowerIdAndFollowingId(followerId, followingId);
    }
}
