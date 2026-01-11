package com.example.monghyang.domain.qna.dto;

import com.example.monghyang.domain.qna.entity.QnaImage;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResQnaImageDto {
    @JsonProperty("image_id")
    private Long imageId;

    @JsonProperty("qna_id")
    private Long qnaId;

    @JsonProperty("image_key")
    private String imageKey;

    @JsonProperty("volume")
    private Integer volume;

    public static ResQnaImageDto from(QnaImage qnaImage) {
        return ResQnaImageDto.builder()
                .imageId(qnaImage.getId())
                .qnaId(qnaImage.getQna().getId())
                .imageKey(qnaImage.getImageKey())
                .volume(qnaImage.getVolume())
                .build();
    }
}
