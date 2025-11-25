package com.example.monghyang.domain.tag.controller;

import com.example.monghyang.domain.global.response.ResponseDataDto;
import com.example.monghyang.domain.tag.dto.ResTagCategoryDto;
import com.example.monghyang.domain.tag.service.TagCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tag-category")
@Tag(name = "태그 카테고리 조회 API", description = "페이지 단위: 10개")
@RequiredArgsConstructor
public class TagCategoryController {
    private final TagCategoryService tagCategoryService;

    // 등록 최신순 조회
    @GetMapping("/latest/{startOffset}")
    @Operation(summary = "태그 카테고리 최신순 조회: 페이징")
    public ResponseEntity<ResponseDataDto<Page<ResTagCategoryDto>>> getCategoryLatest(@PathVariable Integer startOffset) {
        return ResponseEntity.ok().body(ResponseDataDto.contentFrom(tagCategoryService.getTagCategoryListLatest(startOffset)));
    }

    // 키워드 조회
    @GetMapping("/keyword/{keyword}/{startOffset}")
    @Operation(summary = "태그 카테고리 키워드 조회: 페이징")
    public ResponseEntity<ResponseDataDto<Page<ResTagCategoryDto>>> getCategoryKeyword(@PathVariable String keyword, @PathVariable Integer startOffset) {
        return ResponseEntity.ok().body(ResponseDataDto.contentFrom(tagCategoryService.getTagCategoryListKeyword(keyword, startOffset)));
    }

}
