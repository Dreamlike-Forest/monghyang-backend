package com.example.monghyang.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class VerifyAuthDto {
    @NotBlank(message = "검증을 위한 비밀번호 필드를 입력해주세요.")
    private String password;
}
