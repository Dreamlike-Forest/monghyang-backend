package com.example.monghyang.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "기존 비밀번호 검증 요청 정보")
public class VerifyAuthDto {
    @Schema(description = "현재 로그인한 회원의 기존 비밀번호입니다.", example = "password1234!", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "검증을 위한 비밀번호 필드를 입력해주세요.")
    private String password;
}
