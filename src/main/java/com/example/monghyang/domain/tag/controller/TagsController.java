package com.example.monghyang.domain.tag.controller;

import com.example.monghyang.domain.global.response.ResponseDataDto;
import com.example.monghyang.domain.tag.dto.ResTagDto;
import com.example.monghyang.domain.tag.service.TagsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tag")
@Tag(name = "태그 조회 API", description = "페이지 단위: 10개")
@RequiredArgsConstructor
public class TagsController {
    private final TagsService tagsService;

    @GetMapping("/latest/{startOffset}")
    @Operation(summary = "모든 태그를 등록 순서 기준 최신순 조회: 페이징")
    public ResponseEntity<ResponseDataDto<Page<ResTagDto>>> getTagLatest(@PathVariable Integer startOffset) {
        return ResponseEntity.ok().body(ResponseDataDto.contentFrom(tagsService.getTagListLatest(startOffset)));
    }

    @GetMapping("/keyword/{keyword}/{startOffset}")
    @Operation(summary = "키워드를 통한 태그 조회: 페이징")
    public ResponseEntity<ResponseDataDto<Page<ResTagDto>>> getTagKeyword(@PathVariable String keyword, @PathVariable Integer startOffset) {
        return ResponseEntity.ok().body(ResponseDataDto.contentFrom(tagsService.getTagListKeyword(keyword, startOffset)));
    }

    @GetMapping("/in-category/{categoryId}/{startOffset}")
    @Operation(summary = "특정 카테고리의 태그 조회: 페이징")
    public ResponseEntity<ResponseDataDto<Page<ResTagDto>>> getTagByCategory(@PathVariable Integer categoryId, @PathVariable Integer startOffset) {
        return ResponseEntity.ok().body(ResponseDataDto.contentFrom(tagsService.getTagListByCategory(categoryId, startOffset)));
    }

}
