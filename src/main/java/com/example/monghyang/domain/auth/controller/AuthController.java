package com.example.monghyang.domain.auth.controller;

import com.example.monghyang.domain.auth.dto.BreweryJoinDto;
import com.example.monghyang.domain.auth.dto.SellerJoinDto;
import com.example.monghyang.domain.auth.dto.VerifyAuthDto;
import com.example.monghyang.domain.global.annotation.auth.LoginUserId;
import com.example.monghyang.domain.global.response.ResponseDataDto;
import com.example.monghyang.domain.auth.dto.JoinDto;
import com.example.monghyang.domain.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "회원가입 및 인증과 관련된 API")
public class AuthController {
    private final AuthService authService;
    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/refresh")
    @Operation(summary = "세션 및 토큰 갱신", description = "중복 로그인 감지 시 로그아웃해야 합니다.")
    public ResponseEntity<ResponseDataDto<Void>> tokenRefresh(HttpServletRequest request, HttpServletResponse response) {
        authService.updateRefreshToken(request, response);
        return ResponseEntity.ok().body(ResponseDataDto.success("세션 및 토큰 갱신에 성공하였습니다."));
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
    @Operation(summary = "양조장 회원의 회원가입(첫번째 이미지: 대표 이미지)", description = "nickname: 양조장 상호명, name: 양조장 대표자명")
    public ResponseEntity<ResponseDataDto<Void>> breweryJoin(@Valid @ModelAttribute BreweryJoinDto breweryJoinDto) {
        authService.breweryJoin(breweryJoinDto);
        return ResponseEntity.ok().body(ResponseDataDto.success("양조장 회원가입이 완료되었습니다."));
    }
}
