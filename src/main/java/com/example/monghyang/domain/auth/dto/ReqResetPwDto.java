package com.example.monghyang.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "비밀번호 초기화 요청 정보")
public class ReqResetPwDto {
    @Schema(description = "비밀번호를 초기화할 회원 이메일입니다.", example = "user@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @Email(message = "이메일 형식이어야 합니다.")
    @NotBlank(message = "이메일을 입력해주세요.")
    private String email;
    @Schema(description = "새 비밀번호입니다.", example = "newPassword1234!", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "새 비밀번호를 입력해주세요.")
    private String newPassword;
}
