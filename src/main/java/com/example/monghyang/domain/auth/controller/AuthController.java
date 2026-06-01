package com.example.monghyang.domain.auth.controller;

import com.example.monghyang.domain.auth.dto.*;
import com.example.monghyang.domain.global.advice.ApplicationErrorDto;
import com.example.monghyang.domain.global.annotation.auth.LoginUserId;
import com.example.monghyang.domain.global.response.ResponseDataDto;
import com.example.monghyang.domain.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "회원가입 및 인증과 관련된 API")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/refresh")
    @Operation(summary = "세션 및 토큰 갱신", description = "중복 로그인 감지 시 로그아웃해야 합니다.")
    public ResponseEntity<ResponseDataDto<Void>> tokenRefresh(HttpServletRequest request, HttpServletResponse response) {
        authService.updateRefreshToken(request, response);
        return ResponseEntity.ok().body(ResponseDataDto.success("세션 및 토큰 갱신에 성공하였습니다."));
    }

    @PostMapping("/reset-pw")
    @Operation(summary = "비밀번호 초기화", description = "추후 이메일 인증 로직 도입 예정")
    public ResponseEntity<ResponseDataDto<Void>> resetPw(@Valid @ModelAttribute ReqResetPwDto dto) {
        authService.resetPassword(dto);
        return ResponseEntity.ok().body(ResponseDataDto.success("비밀번호가 초기화되었습니다. 로그인 해주세요."));
    }

    @GetMapping("/check-email/{email}")
    @Operation(summary = "이메일 중복체크")
    public ResponseEntity<ResponseDataDto<Void>> checkEmail(@PathVariable String email) {
        authService.checkEmail(email);
        return ResponseEntity.ok().body(ResponseDataDto.success("사용할 수 있는 이메일입니다."));
    }

    @PostMapping("/verify-pw")
    @Operation(summary = "기존 비밀번호 검증 API", description = "정보 수정 등의 동작을 수행하기 전과 같은 상황에서 사용")
    public ResponseEntity<ResponseDataDto<Void>> checkPassword(@LoginUserId Long userId, @Valid @ModelAttribute VerifyAuthDto verifyAuthDto) {
        authService.checkPassword(userId, verifyAuthDto);
        return ResponseEntity.ok().body(ResponseDataDto.success("비밀번호가 일치합니다."));
    }

    @PostMapping(value = "/common-join", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "일반 회원의 플랫폼 회원가입")
    public ResponseEntity<ResponseDataDto<Void>> commonJoin(@Valid @ModelAttribute JoinDto joinDto) {
        authService.commonJoin(joinDto);
        return ResponseEntity.ok().body(ResponseDataDto.success("회원가입이 완료되었습니다."));
    }

    @PostMapping(value = "/seller-join", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "판매자 회원의 회원가입", description = "nickname: 판매자 상호명, name: 판매자 대표자명")
    public ResponseEntity<ResponseDataDto<Void>> sellerJoin(@Valid @ModelAttribute SellerJoinDto sellerJoinDto) {
        authService.sellerJoin(sellerJoinDto);
        return ResponseEntity.ok().body(ResponseDataDto.success("판매자 회원가입이 완료되었습니다."));
    }

    @PostMapping(value = "/brewery-join", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "양조장 회원의 회원가입",
            description = "양조장 회원 계정과 양조장 정보를 함께 생성합니다. nickname은 양조장 상호명, name은 대표자명으로 저장되며, 이미지 순서 seq=1이 대표 이미지입니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "양조장 회원가입 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class),
                            examples = @ExampleObject(value = "{\"status\":200,\"message\":\"양조장 회원가입이 완료되었습니다.\"}"))),
            @ApiResponse(responseCode = "400", description = "필수 입력값, 운영/휴게시간, 이미지 형식 또는 이미지 순서가 올바르지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "역할 또는 지역 정보가 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "409", description = "이메일 중복, 약관 미동의, 이미지 순서 중복 등으로 가입할 수 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseEntity<ResponseDataDto<Void>> breweryJoin(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "양조장 회원가입 요청입니다. 파일이 포함될 수 있으므로 multipart/form-data로 전송합니다.",
                    required = true,
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE, schema = @Schema(implementation = BreweryJoinDto.class))
            )
            @Valid @ModelAttribute BreweryJoinDto breweryJoinDto
    ) {
        authService.breweryJoin(breweryJoinDto);
        return ResponseEntity.ok().body(ResponseDataDto.success("양조장 회원가입이 완료되었습니다."));
    }
}
