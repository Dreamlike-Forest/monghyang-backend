package com.example.monghyang.domain.qna.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@Schema(description = "1:1 문의 작성/수정 요청 정보")
public class ReqQnaDto {
    @Schema(description = "문의 제목입니다.", example = "예약 변경 문의")
    private String qnaTitle;
    @Schema(description = "문의 본문입니다.", example = "예약 날짜 변경이 가능한지 문의드립니다.")
    private String content;
    @Schema(description = "문의 첨부 이미지 파일 목록입니다.", type = "array", nullable = true)
    private List<MultipartFile> images;
}
