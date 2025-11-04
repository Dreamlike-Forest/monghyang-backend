package com.example.monghyang.domain.community.dto;

import com.example.monghyang.domain.community.entity.ImageCommunity;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResImageCommunityDto {
    @JsonProperty("image_community_id")
    private Long imageCommunityId;

    @JsonProperty("community_id")
    private Long communityId;

    @JsonProperty("image_num")
    private Integer imageNum;

    @JsonProperty("image_url")
    private String imageUrl;

    public static ResImageCommunityDto from(ImageCommunity imageCommunity) {
        return ResImageCommunityDto.builder()
                .imageCommunityId(imageCommunity.getId())
                .communityId(imageCommunity.getCommunity().getId())
                .imageNum(imageCommunity.getImageNum())
                .imageUrl(imageCommunity.getImageUrl())
                .build();
    }
}
