package com.example.monghyang.domain.community.service;

import com.example.monghyang.domain.community.dto.*;
import com.example.monghyang.domain.community.entity.Community;
import com.example.monghyang.domain.community.repository.CommunityRepository;
import com.example.monghyang.domain.global.advice.ApplicationError;
import com.example.monghyang.domain.global.advice.ApplicationException;
import com.example.monghyang.domain.users.entity.Users;
import com.example.monghyang.domain.users.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommunityService {
    private final CommunityRepository communityRepository;
    private final UsersRepository usersRepository;
    private final ImageCommunityService imageCommunityService;

    @Transactional
    public ResCommunityDto createCommunity(Long userId, ReqCommunityDto dto) {
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(ApplicationError.USER_NOT_FOUND));

        Community community = Community.builder()
                .user(user)
                .title(dto.getTitle())
                .category(dto.getCategory())
                .subCategory(dto.getSubCategory())
                .productName(dto.getProductName())
                .breweryName(dto.getBreweryName())
                .star(dto.getStar())
                .detail(dto.getDetail())
                .tags(dto.getTags())
                .build();

        Community saved = communityRepository.save(community);

        // 이미지 업로드 처리
        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            for (int i = 0; i < dto.getImages().size(); i++) {
                if (!dto.getImages().get(i).isEmpty()) {
                    imageCommunityService.uploadImage(saved.getId(), i + 1, dto.getImages().get(i));
                }
            }
        }

        return ResCommunityDto.from(saved);
    }

    public List<ResCommunityListDto> getAllCommunities() {
        return communityRepository.findByIsDeletedFalseOrderByCreatedAtDesc()
                .stream()
                .map(ResCommunityListDto::from)
                .collect(Collectors.toList());
    }

    public List<ResCommunityListDto> getCommunitiesByCategory(String category) {
        return communityRepository.findByCategoryAndIsDeletedFalseOrderByCreatedAtDesc(category)
                .stream()
                .map(ResCommunityListDto::from)
                .collect(Collectors.toList());
    }

    public List<ResCommunityListDto> getCommunitiesByUser(Long userId) {
        return communityRepository.findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(userId)
                .stream()
                .map(ResCommunityListDto::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public ResCommunityDto getCommunityById(Long communityId) {
        Community community = communityRepository.findByIdAndIsDeletedFalse(communityId)
                .orElseThrow(() -> new ApplicationException(ApplicationError.COMMUNITY_NOT_FOUND));

        community.increaseViewCount();
        return ResCommunityDto.from(community);
    }

    @Transactional
    public ResCommunityDto updateCommunity(Long userId, Long communityId, ReqCommunityDto dto) {
        Community community = communityRepository.findByIdAndIsDeletedFalse(communityId)
                .orElseThrow(() -> new ApplicationException(ApplicationError.COMMUNITY_NOT_FOUND));

        if (!community.getUser().getId().equals(userId)) {
            throw new ApplicationException(ApplicationError.FORBIDDEN);
        }

        if (dto.getTitle() != null) community.updateTitle(dto.getTitle());
        if (dto.getCategory() != null) community.updateCategory(dto.getCategory());
        if (dto.getSubCategory() != null) community.updateSubCategory(dto.getSubCategory());
        if (dto.getProductName() != null) community.updateProductName(dto.getProductName());
        if (dto.getBreweryName() != null) community.updateBreweryName(dto.getBreweryName());
        if (dto.getStar() != null) community.updateStar(dto.getStar());
        if (dto.getDetail() != null) community.updateDetail(dto.getDetail());
        if (dto.getTags() != null) community.updateTags(dto.getTags());

        // 이미지 업로드 처리 (새로운 이미지가 있는 경우)
        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            for (int i = 0; i < dto.getImages().size(); i++) {
                if (!dto.getImages().get(i).isEmpty()) {
                    imageCommunityService.uploadImage(communityId, i + 1, dto.getImages().get(i));
                }
            }
        }

        return ResCommunityDto.from(community);
    }

    @Transactional
    public void deleteCommunity(Long userId, Long communityId) {
        Community community = communityRepository.findByIdAndIsDeletedFalse(communityId)
                .orElseThrow(() -> new ApplicationException(ApplicationError.COMMUNITY_NOT_FOUND));

        if (!community.getUser().getId().equals(userId)) {
            throw new ApplicationException(ApplicationError.FORBIDDEN);
        }

        // 게시글에 연결된 이미지 모두 삭제
        imageCommunityService.deleteAllImagesByCommunity(communityId);

        community.setDeleted();
    }

    @Transactional
    public void likeCommunity(Long communityId) {
        Community community = communityRepository.findByIdAndIsDeletedFalse(communityId)
                .orElseThrow(() -> new ApplicationException(ApplicationError.COMMUNITY_NOT_FOUND));

        community.increaseLikes();
    }

    @Transactional
    public void unlikeCommunity(Long communityId) {
        Community community = communityRepository.findByIdAndIsDeletedFalse(communityId)
                .orElseThrow(() -> new ApplicationException(ApplicationError.COMMUNITY_NOT_FOUND));

        community.decreaseLikes();
    }
}
