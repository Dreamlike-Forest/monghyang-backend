package com.example.monghyang.domain.qna.controller;

import com.example.monghyang.domain.global.annotation.auth.LoginUserId;
import com.example.monghyang.domain.global.response.ResponseDataDto;
import com.example.monghyang.domain.qna.dto.PageResponseDto;
import com.example.monghyang.domain.qna.dto.ReqQnaDto;
import com.example.monghyang.domain.qna.dto.ResQnaDto;
import com.example.monghyang.domain.qna.dto.ResQnaListDto;
import com.example.monghyang.domain.qna.service.QnaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/qna")
@RequiredArgsConstructor
@Tag(name = "Qna", description = "1:1 문의 API")
public class QnaController {
    private final QnaService qnaService;

    @PostMapping
    @Operation(summary = "문의 등록", description = "1:1 문의를 등록합니다.")
    public ResponseDataDto<ResQnaDto> createQna(
            @LoginUserId Long userId,
            @ModelAttribute ReqQnaDto dto) {
        ResQnaDto result = qnaService.createQna(userId, dto);
        return ResponseDataDto.contentFrom(result);
    }

    @GetMapping("/my/{page}")
    @Operation(summary = "나의 문의 내역 조회", description = "나의 문의 내역을 페이징하여 조회합니다. (10개씩)")
    public ResponseDataDto<PageResponseDto<ResQnaListDto>> getMyQnas(
            @LoginUserId Long userId,
            @PathVariable Integer page) {
        PageResponseDto<ResQnaListDto> result = qnaService.getMyQnas(userId, page);
        return ResponseDataDto.contentFrom(result);
    }

    @GetMapping("/{qnaId}")
    @Operation(summary = "문의 상세 조회", description = "본인의 문의 상세 정보를 조회합니다.")
    public ResponseDataDto<ResQnaDto> getQnaById(
            @LoginUserId Long userId,
            @PathVariable Long qnaId) {
        ResQnaDto result = qnaService.getQnaById(userId, qnaId);
        return ResponseDataDto.contentFrom(result);
    }

    @PostMapping("/{qnaId}")
    @Operation(summary = "문의 수정", description = "문의를 수정합니다.")
    public ResponseDataDto<ResQnaDto> updateQna(
            @LoginUserId Long userId,
            @PathVariable Long qnaId,
            @ModelAttribute ReqQnaDto dto) {
        ResQnaDto result = qnaService.updateQna(userId, qnaId, dto);
        return ResponseDataDto.contentFrom(result);
    }

    @DeleteMapping("/{qnaId}")
    @Operation(summary = "문의 삭제", description = "문의를 삭제합니다.")
    public ResponseDataDto<Void> deleteQna(
            @LoginUserId Long userId,
            @PathVariable Long qnaId) {
        qnaService.deleteQna(userId, qnaId);
        return ResponseDataDto.success("문의가 삭제되었습니다.");
    }
}
