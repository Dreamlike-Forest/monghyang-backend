package com.example.monghyang.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class QuitRequestDto {
    @NotBlank(message = "회원 탈퇴를 하기위해 기존 비밀번호를 입력하여야 합니다.")
    private String password;
}
