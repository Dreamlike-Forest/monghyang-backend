package com.example.monghyang.domain.qna.controller;

import com.example.monghyang.domain.global.annotation.auth.LoginUserId;
import com.example.monghyang.domain.global.response.ResponseDataDto;
import com.example.monghyang.domain.qna.dto.PageResponseDto;
import com.example.monghyang.domain.qna.dto.ReqQnaAnswerDto;
import com.example.monghyang.domain.qna.dto.ResQnaDto;
import com.example.monghyang.domain.qna.dto.ResQnaListDto;
import com.example.monghyang.domain.qna.service.QnaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/qna-priv")
@RequiredArgsConstructor
@Tag(name = "Qna Admin", description = "1:1 문의 관리자 API")
public class QnaPrivController {
    private final QnaService qnaService;

    @GetMapping("/{page}")
    @Operation(summary = "전체 문의 목록 조회", description = "전체 문의 목록을 페이징하여 조회합니다. (10개씩)")
    public ResponseDataDto<PageResponseDto<ResQnaListDto>> getAllQnas(
            @PathVariable Integer page) {
        PageResponseDto<ResQnaListDto> result = qnaService.getAllQnas(page);
        return ResponseDataDto.contentFrom(result);
    }

    @GetMapping("/detail/{qnaId}")
    @Operation(summary = "문의 상세 조회", description = "문의 상세 정보를 조회합니다.")
    public ResponseDataDto<ResQnaDto> getQnaById(
            @PathVariable Long qnaId) {
        ResQnaDto result = qnaService.getQnaByIdForAdmin(qnaId);
        return ResponseDataDto.contentFrom(result);
    }

    @PostMapping("/{qnaId}/answer")
    @Operation(summary = "문의 답변 등록", description = "문의에 답변을 등록합니다.")
    public ResponseDataDto<ResQnaDto> answerQna(
            @LoginUserId Long adminUserId,
            @PathVariable Long qnaId,
            @RequestBody ReqQnaAnswerDto dto) {
        ResQnaDto result = qnaService.answerQna(adminUserId, qnaId, dto);
        return ResponseDataDto.contentFrom(result);
    }
}
