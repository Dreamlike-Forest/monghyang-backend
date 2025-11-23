package com.example.monghyang.domain.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqResetPwDto {
    @Email(message = "이메일 형식이어야 합니다.")
    @NotBlank(message = "이메일을 입력해주세요.")
    private String email;
    @NotBlank(message = "새 비밀번호를 입력해주세요.")
    private String newPassword;
}
