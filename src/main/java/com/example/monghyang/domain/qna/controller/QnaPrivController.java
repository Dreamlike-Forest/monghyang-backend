package com.example.monghyang.domain.qna.controller;

import com.example.monghyang.domain.global.annotation.auth.LoginUserId;
import com.example.monghyang.domain.global.advice.ApplicationErrorDto;
import com.example.monghyang.domain.global.response.ResponseDataDto;
import com.example.monghyang.domain.qna.dto.PageResponseDto;
import com.example.monghyang.domain.qna.dto.ReqQnaAnswerDto;
import com.example.monghyang.domain.qna.dto.ResQnaDto;
import com.example.monghyang.domain.qna.dto.ResQnaListDto;
import com.example.monghyang.domain.qna.service.QnaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/qna-priv")
@RequiredArgsConstructor
@Tag(name = "Qna Admin", description = "1:1 문의 관리자 API")
@SecurityRequirement(name = "SessionID")
public class QnaPrivController {
    private final QnaService qnaService;

    @GetMapping("/{page}")
    @Operation(summary = "전체 문의 목록 조회", description = "전체 문의 목록을 페이징하여 조회합니다. (10개씩)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "전체 문의 목록 조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "400", description = "페이지 번호가 올바르지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "401", description = "세션 인증 정보가 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseDataDto<PageResponseDto<ResQnaListDto>> getAllQnas(
            @PathVariable Integer page) {
        PageResponseDto<ResQnaListDto> result = qnaService.getAllQnas(page);
        return ResponseDataDto.contentFrom(result);
    }

    @GetMapping("/detail/{qnaId}")
    @Operation(summary = "문의 상세 조회", description = "문의 상세 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "관리자 문의 상세 조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "401", description = "세션 인증 정보가 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "문의가 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseDataDto<ResQnaDto> getQnaById(
            @PathVariable Long qnaId) {
        ResQnaDto result = qnaService.getQnaByIdForAdmin(qnaId);
        return ResponseDataDto.contentFrom(result);
    }

    @PostMapping("/{qnaId}/answer")
    @Operation(summary = "문의 답변 등록", description = "문의에 답변을 등록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "문의 답변 등록 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "401", description = "세션 인증 정보가 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "문의가 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseDataDto<ResQnaDto> answerQna(
            @Parameter(hidden = true) @LoginUserId Long adminUserId,
            @PathVariable Long qnaId,
            @RequestBody ReqQnaAnswerDto dto) {
        ResQnaDto result = qnaService.answerQna(adminUserId, qnaId, dto);
        return ResponseDataDto.contentFrom(result);
    }
}
