package com.example.monghyang.domain.community.service;

import com.example.monghyang.domain.community.dto.ResImageCommunityDto;
import com.example.monghyang.domain.community.entity.Community;
import com.example.monghyang.domain.community.entity.ImageCommunity;
import com.example.monghyang.domain.community.repository.CommunityRepository;
import com.example.monghyang.domain.community.repository.ImageCommunityRepository;
import com.example.monghyang.domain.global.advice.ApplicationError;
import com.example.monghyang.domain.global.advice.ApplicationException;
import com.example.monghyang.domain.image.service.ImageType;
import com.example.monghyang.domain.image.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ImageCommunityService {
    private final ImageCommunityRepository imageCommunityRepository;
    private final CommunityRepository communityRepository;
    private final StorageService storageService;

    @Transactional
    public ResImageCommunityDto uploadImage(Long communityId, Integer imageNum, MultipartFile file) {
        Community community = communityRepository.findByIdAndIsDeletedFalse(communityId)
                .orElseThrow(() -> new ApplicationException(ApplicationError.COMMUNITY_NOT_FOUND));

        // 이미지 파일 저장 (AWS S3 or Local)
        String imageKey = storageService.upload(file, ImageType.COMMUNITY_IMAGE);

        // ImageCommunity 엔티티 생성
        ImageCommunity imageCommunity = ImageCommunity.builder()
                .community(community)
                .imageNum(imageNum)
                .imageUrl(imageKey)
                .build();

        ImageCommunity saved = imageCommunityRepository.save(imageCommunity);
        return ResImageCommunityDto.from(saved);
    }

    public List<ResImageCommunityDto> getImagesByCommunity(Long communityId) {
        return imageCommunityRepository.findByCommunityIdOrderByImageNumAsc(communityId)
                .stream()
                .map(ResImageCommunityDto::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteImage(Long imageId) {
        ImageCommunity imageCommunity = imageCommunityRepository.findById(imageId)
                .orElseThrow(() -> new ApplicationException(ApplicationError.IMAGE_NOT_FOUND));

        // 스토리지에서 이미지 삭제
        storageService.remove(imageCommunity.getImageUrl());

        // DB에서 삭제
        imageCommunityRepository.delete(imageCommunity);
    }

    @Transactional
    public void deleteAllImagesByCommunity(Long communityId) {
        List<ImageCommunity> images = imageCommunityRepository.findByCommunityIdOrderByImageNumAsc(communityId);

        for (ImageCommunity image : images) {
            storageService.remove(image.getImageUrl());
        }

        imageCommunityRepository.deleteByCommunityId(communityId);
    }
}
