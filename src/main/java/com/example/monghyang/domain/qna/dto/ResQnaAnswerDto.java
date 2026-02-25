package com.example.monghyang.domain.qna.dto;

import com.example.monghyang.domain.qna.entity.QnaAnswer;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ResQnaAnswerDto {
    @JsonProperty("answer_id")
    private Long answerId;

    @JsonProperty("qna_id")
    private Long qnaId;

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("content")
    private String content;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    public static ResQnaAnswerDto from(QnaAnswer qnaAnswer) {
        return ResQnaAnswerDto.builder()
                .answerId(qnaAnswer.getId())
                .qnaId(qnaAnswer.getQna().getId())
                .userId(qnaAnswer.getUser().getId())
                .content(qnaAnswer.getContent())
                .createdAt(qnaAnswer.getCreatedAt())
                .build();
    }
}
