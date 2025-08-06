package com.example.monghyang.domain.users.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class JoinDto {
    @Email
    @NotBlank
    private String email;
    @NotBlank
    private String password;
    @NotBlank
    private String nickname;
    @NotBlank
    private String name;
    @NotBlank
    private String phone;
    @NotNull
    private LocalDate birth;
    @NotNull
    private String gender; // false: 남자, true: 여자
    @NotBlank
    private String address;
    @NotBlank
    private String address_detail;
    @NotNull
    private Boolean is_agreed; // 약관 동의여부. 반드시 사용자로부터 약관동의를 받아야 하므로, 항상 true여야 하는 필드.
}
