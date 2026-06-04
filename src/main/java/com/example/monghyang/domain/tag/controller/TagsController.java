package com.example.monghyang.domain.tag.controller;

import com.example.monghyang.domain.global.advice.ApplicationErrorDto;
import com.example.monghyang.domain.global.response.ResponseDataDto;
import com.example.monghyang.domain.tag.dto.ResTagDto;
import com.example.monghyang.domain.tag.service.TagsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "태그 최신순 조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "400", description = "페이지 시작 위치가 올바르지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseEntity<ResponseDataDto<Page<ResTagDto>>> getTagLatest(@PathVariable Integer startOffset) {
        return ResponseEntity.ok().body(ResponseDataDto.contentFrom(tagsService.getTagListLatest(startOffset)));
    }

    @GetMapping("/keyword/{keyword}/{startOffset}")
    @Operation(summary = "키워드를 통한 태그 조회: 페이징")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "태그 키워드 조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "400", description = "키워드 또는 페이지 시작 위치가 올바르지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseEntity<ResponseDataDto<Page<ResTagDto>>> getTagKeyword(@PathVariable String keyword, @PathVariable Integer startOffset) {
        return ResponseEntity.ok().body(ResponseDataDto.contentFrom(tagsService.getTagListKeyword(keyword, startOffset)));
    }

    @GetMapping("/in-category/{categoryId}/{startOffset}")
    @Operation(summary = "특정 카테고리의 태그 조회: 페이징")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "카테고리별 태그 조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "400", description = "카테고리 식별자 또는 페이지 시작 위치가 올바르지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "태그 카테고리 정보가 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseEntity<ResponseDataDto<Page<ResTagDto>>> getTagByCategory(@PathVariable Integer categoryId, @PathVariable Integer startOffset) {
        return ResponseEntity.ok().body(ResponseDataDto.contentFrom(tagsService.getTagListByCategory(categoryId, startOffset)));
    }

}
