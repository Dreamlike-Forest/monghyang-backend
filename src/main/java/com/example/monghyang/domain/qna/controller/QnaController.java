package com.example.monghyang.domain.qna.controller;

import com.example.monghyang.domain.global.annotation.auth.LoginUserId;
import com.example.monghyang.domain.global.advice.ApplicationErrorDto;
import com.example.monghyang.domain.global.response.ResponseDataDto;
import com.example.monghyang.domain.qna.dto.PageResponseDto;
import com.example.monghyang.domain.qna.dto.ReqQnaDto;
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
@RequestMapping("/api/qna")
@RequiredArgsConstructor
@Tag(name = "Qna", description = "1:1 문의 API")
public class QnaController {
    private final QnaService qnaService;

    @PostMapping
    @Operation(summary = "문의 등록", description = "1:1 문의를 등록합니다.", security = @SecurityRequirement(name = "SessionID"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "문의 등록 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "400", description = "문의 입력값 또는 이미지 요청이 올바르지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "401", description = "세션 인증 정보가 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseDataDto<ResQnaDto> createQna(
            @Parameter(hidden = true) @LoginUserId Long userId,
            @ModelAttribute ReqQnaDto dto) {
        ResQnaDto result = qnaService.createQna(userId, dto);
        return ResponseDataDto.contentFrom(result);
    }

    @GetMapping("/my/{page}")
    @Operation(summary = "나의 문의 내역 조회", description = "나의 문의 내역을 페이징하여 조회합니다. (10개씩)", security = @SecurityRequirement(name = "SessionID"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "나의 문의 내역 조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "400", description = "페이지 번호가 올바르지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "401", description = "세션 인증 정보가 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseDataDto<PageResponseDto<ResQnaListDto>> getMyQnas(
            @Parameter(hidden = true) @LoginUserId Long userId,
            @PathVariable Integer page) {
        PageResponseDto<ResQnaListDto> result = qnaService.getMyQnas(userId, page);
        return ResponseDataDto.contentFrom(result);
    }

    @GetMapping("/{qnaId}")
    @Operation(summary = "문의 상세 조회", description = "본인의 문의 상세 정보를 조회합니다.", security = @SecurityRequirement(name = "SessionID"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "문의 상세 조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "401", description = "세션 인증 정보가 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "403", description = "문의 조회 권한이 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "문의 정보가 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseDataDto<ResQnaDto> getQnaById(
            @Parameter(hidden = true) @LoginUserId Long userId,
            @PathVariable Long qnaId) {
        ResQnaDto result = qnaService.getQnaById(userId, qnaId);
        return ResponseDataDto.contentFrom(result);
    }

    @PostMapping("/{qnaId}")
    @Operation(summary = "문의 수정", description = "문의를 수정합니다.", security = @SecurityRequirement(name = "SessionID"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "문의 수정 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "400", description = "문의 수정 입력값 또는 이미지 요청이 올바르지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "401", description = "세션 인증 정보가 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "403", description = "문의 수정 권한이 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "문의 정보가 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseDataDto<ResQnaDto> updateQna(
            @Parameter(hidden = true) @LoginUserId Long userId,
            @PathVariable Long qnaId,
            @ModelAttribute ReqQnaDto dto) {
        ResQnaDto result = qnaService.updateQna(userId, qnaId, dto);
        return ResponseDataDto.contentFrom(result);
    }

    @DeleteMapping("/{qnaId}")
    @Operation(summary = "문의 삭제", description = "문의를 삭제합니다.", security = @SecurityRequirement(name = "SessionID"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "문의 삭제 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "401", description = "세션 인증 정보가 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "403", description = "문의 삭제 권한이 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "문의 정보가 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseDataDto<Void> deleteQna(
            @Parameter(hidden = true) @LoginUserId Long userId,
            @PathVariable Long qnaId) {
        qnaService.deleteQna(userId, qnaId);
        return ResponseDataDto.success("문의가 삭제되었습니다.");
    }
}
