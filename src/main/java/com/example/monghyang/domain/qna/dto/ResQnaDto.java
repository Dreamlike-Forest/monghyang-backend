package com.example.monghyang.domain.qna.dto;

import com.example.monghyang.domain.qna.entity.Qna;
import com.example.monghyang.domain.qna.entity.QnaAnswer;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ResQnaDto {
    @JsonProperty("qna_id")
    private Long qnaId;

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("qna_title")
    private String qnaTitle;

    @JsonProperty("content")
    private String content;

    @JsonProperty("images")
    private List<ResQnaImageDto> images;

    @JsonProperty("is_complete")
    private Boolean isComplete;

    @JsonProperty("is_deleted")
    private Boolean isDeleted;

    @JsonProperty("answer")
    private ResQnaAnswerDto answer;

    public static ResQnaDto from(Qna qna, List<ResQnaImageDto> images, QnaAnswer qnaAnswer) {
        return ResQnaDto.builder()
                .qnaId(qna.getId())
                .userId(qna.getUser().getId())
                .qnaTitle(qna.getQnaTitle())
                .content(qna.getContent())
                .images(images)
                .isComplete(qna.getIsComplete())
                .isDeleted(qna.getIsDeleted())
                .answer(qnaAnswer != null ? ResQnaAnswerDto.from(qnaAnswer) : null)
                .build();
    }
}
