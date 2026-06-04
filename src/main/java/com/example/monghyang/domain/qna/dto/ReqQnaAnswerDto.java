package com.example.monghyang.domain.qna.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "1:1 문의 답변 등록 요청 정보")
public class ReqQnaAnswerDto {
    @Schema(description = "관리자 답변 본문입니다.", example = "예약 변경은 예약일 하루 전까지 가능합니다.")
    private String content;
}
