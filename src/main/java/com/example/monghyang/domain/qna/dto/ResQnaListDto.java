package com.example.monghyang.domain.qna.dto;

import com.example.monghyang.domain.qna.entity.Qna;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResQnaListDto {
    @JsonProperty("qna_id")
    private Long qnaId;

    @JsonProperty("qna_title")
    private String qnaTitle;

    @JsonProperty("is_complete")
    private Boolean isComplete;

    public static ResQnaListDto from(Qna qna) {
        return ResQnaListDto.builder()
                .qnaId(qna.getId())
                .qnaTitle(qna.getQnaTitle())
                .isComplete(qna.getIsComplete())
                .build();
    }
}
