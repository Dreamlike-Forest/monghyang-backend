package com.example.monghyang.domain.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@Schema(description = "커뮤니티 게시글 작성/수정 요청 정보")
public class ReqCommunityDto {
    @Schema(description = "커뮤니티 게시글 제목입니다.", example = "막걸리 체험 후기")
    private String title;
    @Schema(description = "커뮤니티 게시글 카테고리입니다.", example = "REVIEW")
    private String category;
    @Schema(description = "커뮤니티 게시글 하위 카테고리입니다.", example = "JOY", nullable = true)
    private String subCategory;
    @Schema(description = "게시글과 연관된 상품명입니다.", example = "전통주 선물세트", nullable = true)
    private String productName;
    @Schema(description = "게시글과 연관된 양조장명입니다.", example = "몽향양조장", nullable = true)
    private String breweryName;
    @Schema(description = "후기 별점입니다.", example = "4.5", nullable = true)
    private Double star;
    @Schema(description = "커뮤니티 게시글 본문입니다.", example = "체험 진행이 친절하고 술 향이 좋았습니다.")
    private String detail;
    @Schema(description = "게시글 태그 문자열입니다.", example = "막걸리,체험,후기", nullable = true)
    private String tags;
    @Schema(description = "게시글 이미지 파일 목록입니다.", type = "array", nullable = true)
    private List<MultipartFile> images;
}
